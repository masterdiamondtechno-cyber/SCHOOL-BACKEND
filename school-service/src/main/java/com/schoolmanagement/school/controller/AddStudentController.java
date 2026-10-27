package com.schoolmanagement.school.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.schoolmanagement.school.dto.AddStudentDTO;
import com.schoolmanagement.school.dto.ChangePasswordDTO;
import com.schoolmanagement.school.dto.ResetPasswordDTO;
import com.schoolmanagement.school.dto.ResetPasswordsBulkDTO;
import com.schoolmanagement.school.dto.UpdatePasswordDTO;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.service.AddStudentService;
import com.schoolmanagement.Config.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/student")
public class AddStudentController {

    private final AddStudentService studentService;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;
    private final JwtUtil jwtUtil;

    public AddStudentController(AddStudentService studentService,
            SchoolRepository schoolRepository,
            AcademicYearRepository academicYearRepository,
            JwtUtil jwtUtil) {
        this.studentService = studentService;
        this.schoolRepository = schoolRepository;
        this.academicYearRepository = academicYearRepository;
        this.jwtUtil = jwtUtil;
    }

    // ==================== Helper ====================
    private Map<String, Object> build(int code, String status, String message, Object data) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", code);
        r.put("status", status);
        r.put("message", message);
        r.put("data", data);
        return r;
    }

    // ----------------------------------------------------------------------------------------------------------

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody(required = false) AddStudentDTO dto) {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            if (dto == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            }

            if ((dto.getFirstName() == null || dto.getFirstName().trim().isEmpty()) &&
                    (dto.getMobileNumber() == null || dto.getMobileNumber().trim().isEmpty()) &&
                    (dto.getEmailId() == null || dto.getEmailId().trim().isEmpty())) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "No data provided in request body", null));
            }

            if (dto.getSchoolId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            }
            if (dto.getAcademicYearId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            }
            if (dto.getStandardId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'standardId' is required", null));
            }
            if (dto.getDivisionId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'divisionId' is required", null));
            }

            studentService.createStudent(dto);

            return ResponseEntity.ok(build(200, "SUCCESS", "Student created successfully", null));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(build(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }

    // --------------------------------------------------------------------------------------------------------------

    @PostMapping("/getbyschoolid/studentid")
    public ResponseEntity<Map<String, Object>> getById(@RequestBody(required = false) AddStudentDTO dto) {

        try {
            if (dto == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            }

            if (dto.getId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            }
            if (dto.getSchoolId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            }
            if (dto.getAcademicYearId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            }

            if (!schoolRepository.existsById(dto.getSchoolId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "School not found", null));
            }
            if (!academicYearRepository.existsById(dto.getAcademicYearId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "Academic Year not found", null));
            }

            AddStudentDTO student = studentService.getStudentByIdAndSchoolId(dto);

            return ResponseEntity.ok(build(200, "SUCCESS", "Student retrieved successfully", student));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error", null));
        }
    }

    // ---------------------------------------------------------------------------------------------------------------

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@RequestBody(required = false) AddStudentDTO dto) {

        try {
            if (dto == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            }

            if (dto.getId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required for update", null));
            }
            if (dto.getSchoolId() == null) {
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "'schoolId' is required for update", null));
            }

            if (!schoolRepository.existsById(dto.getSchoolId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "School not found", null));
            }

            studentService.updateStudentByIdAndSchoolId(dto);

            return ResponseEntity.ok(build(200, "SUCCESS", "Student updated successfully", null));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error", null));
        }
    }

    // ---------------------------------------

    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestBody(required = false) AddStudentDTO dto) {

        try {
            if (dto == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            }

            if (dto.getId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            }
            if (dto.getSchoolId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            }

            if (!schoolRepository.existsById(dto.getSchoolId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "School not found", null));
            }

            studentService.deleteStudentByIdAndSchoolId(dto);

            return ResponseEntity.ok(build(200, "SUCCESS", "Student deleted successfully", null));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error", null));
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    @PostMapping("/active")
    public ResponseEntity<Map<String, Object>> getActive(@RequestBody(required = false) AddStudentDTO dto) {

        try {
            if (dto == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            }

            if (dto.getSchoolId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            }
            if (dto.getAcademicYearId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            }

            if (!schoolRepository.existsById(dto.getSchoolId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "School not found", null));
            }
            if (!academicYearRepository.existsById(dto.getAcademicYearId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "Academic Year not found", null));
            }

            List<AddStudentDTO> students = studentService.getAllActiveStudentsBySchoolId(dto);

            if (students.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "No active students found for this schoolId", null));
            }

            return ResponseEntity.ok(build(200, "SUCCESS", "Active students retrieved successfully", students));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error", null));
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    @PostMapping("/inactive")
    public ResponseEntity<Map<String, Object>> getInactive(@RequestBody(required = false) AddStudentDTO dto) {

        try {
            if (dto == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            }

            if (dto.getSchoolId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            }
            if (dto.getAcademicYearId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            }

            if (!schoolRepository.existsById(dto.getSchoolId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "School not found", null));
            }
            if (!academicYearRepository.existsById(dto.getAcademicYearId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "Academic Year not found", null));
            }

            List<AddStudentDTO> students = studentService.getAllInactiveStudentsBySchoolId(dto);

            if (students.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "No inactive students found for this schoolId", null));
            }

            return ResponseEntity.ok(build(200, "SUCCESS", "Inactive students retrieved successfully", students));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error", null));
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    @PostMapping("/count/active")
    public ResponseEntity<Map<String, Object>> countActive(@RequestBody(required = false) AddStudentDTO dto) {

        try {
            if (dto == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            }

            if (dto.getSchoolId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            }
            if (dto.getAcademicYearId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            }

            if (!schoolRepository.existsById(dto.getSchoolId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "School not found", null));
            }
            if (!academicYearRepository.existsById(dto.getAcademicYearId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "Academic Year not found", null));
            }

            Long count = studentService.countActiveStudentsBySchoolId(dto);

            return ResponseEntity.ok(build(200, "SUCCESS", "Active students count retrieved successfully", count));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error", null));
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    @PostMapping("/count/inactive")
    public ResponseEntity<Map<String, Object>> countInactive(@RequestBody(required = false) AddStudentDTO dto) {

        try {
            if (dto == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            }

            if (dto.getSchoolId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            }
            if (dto.getAcademicYearId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            }

            if (!schoolRepository.existsById(dto.getSchoolId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "School not found", null));
            }
            if (!academicYearRepository.existsById(dto.getAcademicYearId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "Academic Year not found", null));
            }

            Long inactiveCount = studentService.countInactiveStudentsBySchoolId(dto);

            return ResponseEntity
                    .ok(build(200, "SUCCESS", "Inactive students count retrieved successfully", inactiveCount));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error", null));
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    @PostMapping("/count/total")
    public ResponseEntity<Map<String, Object>> countTotal(@RequestBody(required = false) AddStudentDTO dto) {

        try {
            if (dto == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            }

            if (dto.getSchoolId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            }
            if (dto.getAcademicYearId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            }

            if (!schoolRepository.existsById(dto.getSchoolId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "School not found", null));
            }
            if (!academicYearRepository.existsById(dto.getAcademicYearId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "Academic Year not found", null));
            }

            Long total = studentService.totalRegisteredStudentsBySchoolId(dto);

            return ResponseEntity.ok(build(200, "SUCCESS", "Total registered students retrieved successfully", total));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error", null));
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------

    @PostMapping("/getBySection")
    public ResponseEntity<Map<String, Object>> getBySection(@RequestBody Map<String, Long> req) {
        try {
            Long schoolId = req.get("schoolId");
            Long standardId = req.get("standardId");
            Long divisionId = req.get("divisionId");

            if (schoolId == null || standardId == null || divisionId == null) {
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "schoolId, standardId, and divisionId are required", null));
            }

            List<AddStudentDTO> students = studentService.getStudentsByDivision(schoolId, standardId, divisionId);

            if (students.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "No students found for this section", null));
            }

            return ResponseEntity.ok(build(200, "SUCCESS", "Students retrieved successfully", students));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error", null));
        }
    }

    @PostMapping("/getall")
    public ResponseEntity<Map<String, Object>> getAllBySchoolId(@RequestBody(required = false) AddStudentDTO dto) {

        try {
            if (dto == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            }

            if (dto.getSchoolId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            }
            if (dto.getAcademicYearId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            }

            if (!schoolRepository.existsById(dto.getSchoolId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "School not found", null));
            }
            if (!academicYearRepository.existsById(dto.getAcademicYearId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "Academic Year not found", null));
            }

            List<AddStudentDTO> students = studentService.getAllStudentsBySchoolId(dto);

            if (students.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "No students found for this schoolId", null));
            }

            return ResponseEntity.ok(build(200, "SUCCESS", "Students retrieved successfully", students));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error", null));
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    @PostMapping("/check/email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestBody(required = false) AddStudentDTO dto) {

        try {
            if (dto == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            }

            if (dto.getSchoolId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            }
            if (dto.getEmailId() == null || dto.getEmailId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'emailId' is required", null));
            }

            if (!schoolRepository.existsById(dto.getSchoolId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "School not found", null));
            }

            boolean exists = studentService.isEmailAlreadyPresent(dto);

            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(build(409, "FAILED", "Email already exists", null));
            } else {
                return ResponseEntity.ok(build(200, "SUCCESS", "Email is available", null));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error", null));
        }
    }

    // ------------------------------------------------------------------------------------------------------------

    @PostMapping("/check/mobile")
    public ResponseEntity<Map<String, Object>> checkMobile(@RequestBody(required = false) AddStudentDTO dto) {

        try {
            if (dto == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            }

            if (dto.getSchoolId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            }
            if (dto.getMobileNumber() == null || dto.getMobileNumber().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'mobileNumber' is required", null));
            }

            if (!schoolRepository.existsById(dto.getSchoolId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "School not found", null));
            }

            boolean exists = studentService.isMobileNumberAlreadyPresent(dto);

            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(build(409, "FAILED", "Mobile number already exists", null));
            } else {
                return ResponseEntity.ok(build(200, "SUCCESS", "Mobile number is available", null));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error", null));
        }
    }

    // -----------------------------------------

    @PostMapping("/update/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@RequestBody(required = false) AddStudentDTO dto) {

        try {
            if (dto == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            }

            if (dto.getId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            }
            if (dto.getSchoolId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            }
            if (dto.getStatus() == null || dto.getStatus().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'status' is required", null));
            }

            String statusValue = dto.getStatus().trim().toUpperCase();
            if (!statusValue.equals("ACTIVE") && !statusValue.equals("INACTIVE")) {
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "status must be ACTIVE or INACTIVE", null));
            }

            if (!schoolRepository.existsById(dto.getSchoolId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "School not found", null));
            }

            dto.setStatus(statusValue);
            studentService.updateStudentStatus(dto);

            return ResponseEntity.ok(build(200, "SUCCESS", "Student status updated successfully", null));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error", null));
        }
    }

    // ==================== CHANGE PASSWORD (Student) ====================

    @PostMapping("/changePassword")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody(required = false) ChangePasswordDTO dto,
                                                              HttpServletRequest request) {

        try {
            if (dto == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            }
            if (dto.getStudentId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required", null));
            }
            if (dto.getSchoolId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            }

            // Industry standard: Verify token identity to prevent IDOR attacks
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Object uidClaim = jwtUtil.extractClaim(token, "uid");
                if (uidClaim != null) {
                    Long tokenStudentId = Long.valueOf(uidClaim.toString());
                    if (!tokenStudentId.equals(dto.getStudentId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(build(403, "FAILED", "Unauthorized: You can only change your own password", null));
                    }
                }
            }
            if (dto.getCurrentPassword() == null || dto.getCurrentPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'currentPassword' is required", null));
            }
            if (dto.getNewPassword() == null || dto.getNewPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'newPassword' is required", null));
            }

            if (dto.getConfirmPassword() == null || dto.getConfirmPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'confirmPassword' is required", null));
            }

            studentService.changePassword(dto);

            return ResponseEntity.ok(build(200, "SUCCESS", "Password changed successfully", null));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(build(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }

    // ==================== UPDATE PASSWORD (Student - after first change)
    // ====================

    @PostMapping("/updatePassword")
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestBody(required = false) UpdatePasswordDTO dto,
                                                              HttpServletRequest request) {

        try {
            if (dto == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            }
            if (dto.getStudentId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required", null));
            }
            if (dto.getSchoolId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            }

            // Industry standard: Verify token identity to prevent IDOR attacks
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Object uidClaim = jwtUtil.extractClaim(token, "uid");
                if (uidClaim != null) {
                    Long tokenStudentId = Long.valueOf(uidClaim.toString());
                    if (!tokenStudentId.equals(dto.getStudentId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(build(403, "FAILED", "Unauthorized: You can only update your own password", null));
                    }
                }
            }
            if (dto.getOldPassword() == null || dto.getOldPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'oldPassword' is required", null));
            }
            if (dto.getNewPassword() == null || dto.getNewPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'newPassword' is required", null));
            }
            if (dto.getConfirmPassword() == null || dto.getConfirmPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'confirmPassword' is required", null));
            }

            studentService.updatePassword(dto);

            return ResponseEntity.ok(build(200, "SUCCESS", "Password updated successfully", null));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(build(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }

    // ==================== RESET PASSWORD (School Admin) ====================

    @PostMapping("/resetPassword")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody(required = false) ResetPasswordDTO dto) {

        try {
            if (dto == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            }
            if (dto.getStudentId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required", null));
            }
            if (dto.getSchoolId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            }

            studentService.resetPassword(dto);

            return ResponseEntity.ok(build(200, "SUCCESS", "Password reset to date of birth successfully", null));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(build(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }

    // ==================== MIGRATE PASSWORDS TO DOB (One-time, School Admin)
    // ====================

    @PostMapping("/migratePasswords")
    public ResponseEntity<Map<String, Object>> migratePasswords(@RequestBody(required = false) Map<String, Long> body) {

        try {
            if (body == null || body.get("schoolId") == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            }

            Long schoolId = body.get("schoolId");

            if (!schoolRepository.existsById(schoolId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "School not found", null));
            }

            int count = studentService.migratePasswordsToDob(schoolId);

            return ResponseEntity.ok(build(200, "SUCCESS",
                    count + " students' passwords migrated to DOB successfully", count));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }

    // ==================== RESET PASSWORD BULK (School Admin) ====================
    @PostMapping("/resetPasswordBulk")
    public ResponseEntity<Map<String, Object>> resetPasswordBulk(@RequestBody(required = false) ResetPasswordsBulkDTO dto) {
        try {
            if (dto == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            }
            if (dto.getSchoolId() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            }
            if (dto.getStudentIds() == null || dto.getStudentIds().isEmpty()) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentIds' list is required and cannot be empty", null));
            }

            int count = studentService.resetPasswordsBulk(dto);

            return ResponseEntity.ok(build(200, "SUCCESS",
                    count + " students' passwords reset to DOB successfully", count));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(build(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }

}
