package com.schoolmanagement.school.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.schoolmanagement.school.dto.AddSubjectDTO;
import com.schoolmanagement.school.dto.AddTeacherDTO;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.service.AddSubjectService;
import com.schoolmanagement.school.service.AddTeacherService;

@RestController
@RequestMapping("/teacher")
public class AddTeacherController {

    @Autowired
    private AddTeacherService teacherService;

    @Autowired
    private AddSubjectService subjectService;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(
            @RequestBody(required = false) AddTeacherDTO teacher) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            // 1️⃣ Request body missing
            if (teacher == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is missing");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 2️⃣ Empty JSON {}
            if ((teacher.getTeacherName() == null || teacher.getTeacherName().trim().isEmpty()) &&
                    (teacher.getMobileNumber() == null || teacher.getMobileNumber().trim().isEmpty()) &&
                    (teacher.getEmailId() == null || teacher.getEmailId().trim().isEmpty())) {

                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "No data provided in request body");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 3️⃣ Save teacher
            teacherService.createTeacher(teacher);

            // 4️⃣ Success
            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "Teacher created successfully");
            response.put("data", null);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("code", 400);
            response.put("status", "FAILED");
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    // ---------------------------------------------------------

    @PostMapping("/getAllBySchoolId")
    public ResponseEntity<Map<String, Object>> getAllBySchoolId(
            @RequestBody Map<String, Long> request) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            Long schoolId = request.get("schoolId");
            Long academicYearId = request.get("academicYearId");

            if (schoolId == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "schoolId is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (academicYearId == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "academicYearId is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (!schoolRepository.existsById(schoolId)) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "School not found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (!academicYearRepository.existsById(academicYearId)) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "Academic Year not found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            List<AddTeacherDTO> teachers = teacherService.getAllBySchoolId(schoolId, academicYearId);

            if (teachers.isEmpty()) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "No teachers found for the given schoolId");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "Teachers fetched successfully");
            response.put("data", teachers);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ===================== GET BY ID + SCHOOL ID =====================
    @PostMapping("/getById")
    public ResponseEntity<Map<String, Object>> getById(
            @RequestBody(required = false) Map<String, Long> request) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            if (request == null || request.isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is missing or empty");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            Long id = request.get("id");
            Long schoolId = request.get("schoolId");
            Long academicYearId = request.get("academicYearId");

            if (id == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'id' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (schoolId == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'schoolId' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (academicYearId == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'academicYearId' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (!schoolRepository.existsById(schoolId)) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "School not found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (!academicYearRepository.existsById(academicYearId)) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "Academic Year not found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            AddTeacherDTO teacher = teacherService.getByIdAndSchoolId(id, schoolId);

            if (teacher == null) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "No teacher found for the given id and schoolId");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "Teacher fetched successfully");
            response.put("data", teacher);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    // ------------------------------------------------------------------

    // ===================== DELETE TEACHER =====================
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteTeacher(
            @RequestBody(required = false) Map<String, Long> request) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            // 1️⃣ Check if request body is missing
            if (request == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is missing");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (request.isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is empty");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            Long id = request.get("id");
            Long schoolId = request.get("schoolId");

            // 2️⃣ Validate required fields
            if (id == null || schoolId == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'id' and 'schoolId' are required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            boolean deleted = teacherService.deleteTeacher(id, schoolId);

            if (!deleted) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "Teacher not found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "Teacher deleted successfully");
            response.put("data", null);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ===================== GET ACTIVE TEACHERS =====================
    @PostMapping("/getActive")
    public ResponseEntity<Map<String, Object>> getActiveTeachers(
            @RequestBody(required = false) Map<String, Long> request) {

        return getTeachersByStatus(request, "ACTIVE");
    }

    // ===================== GET INACTIVE TEACHERS =====================
    @PostMapping("/getInactive")
    public ResponseEntity<Map<String, Object>> getInactiveTeachers(
            @RequestBody(required = false) Map<String, Long> request) {

        return getTeachersByStatus(request, "INACTIVE");
    }

    // ===================== COMMON METHOD FOR ACTIVE / INACTIVE
    // =====================
    private ResponseEntity<Map<String, Object>> getTeachersByStatus(
            @RequestBody(required = false) Map<String, Long> request, String status) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            if (request == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is missing");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (request.isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is empty");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            Long schoolId = request.get("schoolId");
            Long academicYearId = request.get("academicYearId");

            if (schoolId == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'schoolId' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (academicYearId == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'academicYearId' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (!schoolRepository.existsById(schoolId)) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "School not found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (!academicYearRepository.existsById(academicYearId)) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "Academic Year not found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            List<AddTeacherDTO> teachersByStatus = teacherService.getTeachersByStatus(schoolId, academicYearId, status);

            if (teachersByStatus.isEmpty()) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "No " + status.toLowerCase() + " teachers found for the given schoolId");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", status + " teachers fetched successfully");
            response.put("data", teachersByStatus);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // -----------------------------------------------------------------------------------------
    // ===================== COUNT ACTIVE TEACHERS =====================
    @PostMapping("/countActive")
    public ResponseEntity<Map<String, Object>> countActiveTeachers(
            @RequestBody(required = false) Map<String, Long> request) {

        return countByStatus(request, "ACTIVE");
    }

    // ===================== COUNT INACTIVE TEACHERS =====================
    @PostMapping("/countInactive")
    public ResponseEntity<Map<String, Object>> countInactiveTeachers(
            @RequestBody(required = false) Map<String, Long> request) {

        return countByStatus(request, "INACTIVE");
    }

    // ===================== COMMON METHOD FOR COUNT =====================
    private ResponseEntity<Map<String, Object>> countByStatus(
            Map<String, Long> request, String status) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            if (request == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is missing");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (request.isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is empty");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            Long schoolId = request.get("schoolId");
            Long academicYearId = request.get("academicYearId");

            if (schoolId == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'schoolId' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (academicYearId == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'academicYearId' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (!schoolRepository.existsById(schoolId)) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "School not found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (!academicYearRepository.existsById(academicYearId)) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "Academic Year not found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            long count = teacherService.countBySchoolIdAndStatus(schoolId, academicYearId, status);

            Map<String, Long> data = new HashMap<>();
            data.put("count", count);

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", status + " teachers count fetched successfully");
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ===================== TOTAL REGISTERED TEACHERS =====================
    @PostMapping("/countTotal")
    public ResponseEntity<Map<String, Object>> countTotalTeachers(
            @RequestBody(required = false) Map<String, Long> request) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            if (request == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is missing");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (request.isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is empty");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            Long schoolId = request.get("schoolId");
            Long academicYearId = request.get("academicYearId");

            if (schoolId == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'schoolId' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (academicYearId == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'academicYearId' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (!schoolRepository.existsById(schoolId)) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "School not found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (!academicYearRepository.existsById(academicYearId)) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "Academic Year not found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            long totalCount = teacherService.countBySchoolId(schoolId, academicYearId);

            Map<String, Long> data = new HashMap<>();
            data.put("totalRegistered", totalCount);

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "Total registered teachers fetched successfully");
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    // ----------------------------------------------------

    // ===================== CHECK MOBILE =====================
    @PostMapping("/checkMobile")
    public ResponseEntity<Map<String, Object>> checkMobile(
            @RequestBody(required = false) Map<String, String> request) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            if (request == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is missing");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (request.isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is empty");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            String schoolIdStr = request.get("schoolId");
            String mobileNumber = request.get("mobileNumber");

            if (schoolIdStr == null || schoolIdStr.trim().isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'schoolId' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (mobileNumber == null || mobileNumber.trim().isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'mobileNumber' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            Long schoolId = Long.parseLong(schoolIdStr);

            boolean exists = teacherService.isMobileExists(schoolId, mobileNumber);

            if (exists) {
                response.put("code", 409);
                response.put("status", "FAILED");
                response.put("message", "Mobile number already exists");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "Mobile number is available");
            response.put("data", null);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ===================== CHECK EMAIL =====================
    @PostMapping("/checkEmail")
    public ResponseEntity<Map<String, Object>> checkEmail(
            @RequestBody(required = false) Map<String, String> request) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            if (request == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is missing");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (request.isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is empty");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            String schoolIdStr = request.get("schoolId");
            String emailId = request.get("emailId");

            if (schoolIdStr == null || schoolIdStr.trim().isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'schoolId' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (emailId == null || emailId.trim().isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'emailId' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            Long schoolId = Long.parseLong(schoolIdStr);

            boolean exists = teacherService.isEmailExists(schoolId, emailId);

            if (exists) {
                response.put("code", 409);
                response.put("status", "FAILED");
                response.put("message", "Email already exists");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "Email is available");
            response.put("data", null);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // -------------------------------------------------------------------
    @PostMapping("/changeStatus")
    public ResponseEntity<Map<String, Object>> changeStatus(
            @RequestBody(required = false) Map<String, String> request) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            // 1. Request body missing
            if (request == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is missing");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 2. Empty JSON
            if (request.isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is empty");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            String idStr = request.get("id");
            String schoolIdStr = request.get("schoolId");
            String status = request.get("status");

            // 3. Validate fields
            if (idStr == null || idStr.trim().isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'id' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (schoolIdStr == null || schoolIdStr.trim().isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'schoolId' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (status == null || status.trim().isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'status' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 4. Validate status value
            status = status.toUpperCase();
            if (!status.equals("ACTIVE") && !status.equals("INACTIVE")) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Status must be either ACTIVE or INACTIVE");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            Long id = Long.parseLong(idStr);
            Long schoolId = Long.parseLong(schoolIdStr);

            teacherService.changeTeacherStatus(id, schoolId, status);

            // 5. Success
            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "Teacher status updated successfully");
            response.put("data", null);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("code", 404);
            response.put("status", "FAILED");
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ------------------------------------------------
    @PostMapping("/getTeacherIdNameBySchoolId")
    public ResponseEntity<Map<String, Object>> getTeacherIdNameBySchoolId(
            @RequestBody(required = false) Map<String, String> request) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            // 1. Request body missing
            if (request == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is missing");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 2. Empty JSON
            if (request.isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is empty");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            String schoolIdStr = request.get("schoolId");
            String academicYearIdStr = request.get("academicYearId");

            // 3. Validation
            if (schoolIdStr == null || schoolIdStr.trim().isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'schoolId' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (academicYearIdStr == null || academicYearIdStr.trim().isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'academicYearId' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            Long schoolId = Long.parseLong(schoolIdStr);
            Long academicYearId = Long.parseLong(academicYearIdStr);

            if (!schoolRepository.existsById(schoolId)) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "School not found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (!academicYearRepository.existsById(academicYearId)) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "Academic Year not found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            List<Map<String, Object>> teachers = teacherService.getTeacherIdAndNameBySchoolId(schoolId, academicYearId);

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "Teacher list fetched successfully");
            response.put("data", teachers);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("code", 404);
            response.put("status", "FAILED");
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // -------------------------------------------------------------------------------------
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateTeacher(
            @RequestBody(required = false) AddTeacherDTO dto) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            // 1. Body missing
            if (dto == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is missing");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 2. ID validation
            if (dto.getId() == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'id' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 3. SchoolId validation
            if (dto.getSchoolId() == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'schoolId' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            teacherService.updateTeacher(dto);

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "Teacher updated successfully");
            response.put("data", null);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("code", 404);
            response.put("status", "FAILED");
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ================= GET ALL SUBJECTS BY TEACHER ID =================
    @PostMapping("/classes")
    public ResponseEntity<Map<String, Object>> getAllByTeacherId(@RequestBody Map<String, Long> req) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Long schoolId = req.get("schoolId");
            Long teacherId = req.get("teacherId");
            Long academicYearId = req.get("academicYearId");

            if (schoolId == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "schoolId is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (teacherId == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "teacherId is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (academicYearId == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "academicYearId is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (!schoolRepository.existsById(schoolId)) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "School not found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            if (!academicYearRepository.existsById(academicYearId)) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "Academic Year not found");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            List<AddSubjectDTO> list = subjectService.getByTeacherId(teacherId, academicYearId);

            String teacherName = "";
            String schoolName = "";
            String academicYearName = "";

            if (!list.isEmpty()) {
                AddSubjectDTO first = list.get(0);
                teacherName = first.getTeacherName() != null ? first.getTeacherName() : "";
                schoolId = first.getSchoolId();
                schoolName = first.getSchoolName() != null ? first.getSchoolName() : "";
                academicYearName = first.getAcademicYearName() != null ? first.getAcademicYearName() : "";
            }

            // Group by class (Standard + Division)
            Map<String, Map<String, Object>> classGroups = new LinkedHashMap<>();
            for (AddSubjectDTO s : list) {
                String classKey = s.getStandardId() + "-" + s.getDivisionId();
                if (!classGroups.containsKey(classKey)) {
                    Map<String, Object> classData = new LinkedHashMap<>();
                    classData.put("standardId", s.getStandardId());
                    classData.put("standard", s.getStandard());
                    classData.put("divisionId", s.getDivisionId());
                    classData.put("divisionName", s.getDivisionName());
                    classData.put("subjects", new ArrayList<Map<String, Object>>());
                    classGroups.put(classKey, classData);
                }

                Map<String, Object> classData = classGroups.get(classKey);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> subjectsList = (List<Map<String, Object>>) classData.get("subjects");

                Map<String, Object> subjectMap = new LinkedHashMap<>();
                subjectMap.put("subjectId", s.getSubjectId());
                subjectMap.put("subjectName", s.getSubjectName());
                subjectMap.put("description", s.getDescription());
                subjectsList.add(subjectMap);
            }

            List<Map<String, Object>> classesArray = new ArrayList<>(classGroups.values());

            Map<String, Object> responseData = new LinkedHashMap<>();
            responseData.put("schoolId", schoolId);
            responseData.put("schoolName", schoolName);
            responseData.put("academicYearId", academicYearId);
            responseData.put("academicYearName", academicYearName);
            responseData.put("teacherId", teacherId);
            responseData.put("teacherName", teacherName);
            responseData.put("classes", classesArray);

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message",
                    list.isEmpty() ? "No classes found for this teacher" : "Classes fetched successfully");
            response.put("data", responseData);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("code", 404);
            response.put("status", "FAILED");
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Something went wrong");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/resetPasswordBulk")
    public ResponseEntity<Map<String, Object>> resetPasswordBulk(@RequestBody(required = false) com.schoolmanagement.school.dto.ResetTeacherPasswordsBulkDTO dto) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            if (dto == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is missing");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getSchoolId() == null) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'schoolId' is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getTeacherIds() == null || dto.getTeacherIds().isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "'teacherIds' list is required and cannot be empty");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            int count = teacherService.resetPasswordsBulk(dto);

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", count + " teachers' passwords reset successfully");
            response.put("data", count);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("code", 400);
            response.put("status", "FAILED");
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
