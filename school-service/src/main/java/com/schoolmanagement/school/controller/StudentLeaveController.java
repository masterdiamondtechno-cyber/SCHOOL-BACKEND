package com.schoolmanagement.school.controller;

import com.schoolmanagement.school.dto.StudentLeaveDTO;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.service.StudentLeaveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student-leave")
public class StudentLeaveController {

    private final StudentLeaveService studentLeaveService;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;

    public StudentLeaveController(StudentLeaveService studentLeaveService,
                                  SchoolRepository schoolRepository,
                                  AcademicYearRepository academicYearRepository) {
        this.studentLeaveService = studentLeaveService;
        this.schoolRepository = schoolRepository;
        this.academicYearRepository = academicYearRepository;
    }

    private Map<String, Object> build(int code, String status, String message, Object data) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", code);
        r.put("status", status);
        r.put("message", message);
        r.put("data", data);
        return r;
    }

    private static final List<String> VALID_LEAVE_TYPES =
            List.of("SICK", "CASUAL", "EMERGENCY", "OTHER");

    // ─────────────────────────────────────────────────────────────────────────
    // STUDENT ENDPOINTS
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> apply(@RequestBody StudentLeaveDTO dto) {
        try {
            if (dto.getFromDate() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'fromDate' is required", null));
            if (dto.getToDate() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'toDate' is required", null));
            if (dto.getLeaveType() != null && !VALID_LEAVE_TYPES.contains(dto.getLeaveType().toUpperCase()))
                return ResponseEntity.badRequest().body(build(400, "FAILED",
                        "leaveType must be one of: SICK, CASUAL, EMERGENCY, OTHER", null));
            if (dto.getAcademicYearId() != null && !academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic year not found", null));

            studentLeaveService.applyLeave(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Leave applied successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(build(409, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancel(@RequestBody StudentLeaveDTO dto) {
        try {
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            studentLeaveService.cancelLeave(dto.getId(), dto.getSchoolId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Leave cancelled successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(build(409, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    @PostMapping("/getMyLeaves")
    public ResponseEntity<Map<String, Object>> getMyLeaves(@RequestBody StudentLeaveDTO dto) {
        try {
            if (dto.getStudentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            List<Map<String, Object>> leaves = studentLeaveService.getMyLeaves(
                    dto.getStudentId(), dto.getSchoolId(), dto.getStatusFilter());

            if (leaves.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No leave records found", null));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("leaves", leaves);
            return ResponseEntity.ok(build(200, "SUCCESS", "Leave history fetched successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    @PostMapping("/getMyStats")
    public ResponseEntity<Map<String, Object>> getMyStats(@RequestBody StudentLeaveDTO dto) {
        try {
            if (dto.getStudentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            Map<String, Object> stats = studentLeaveService.getMyStats(
                    dto.getStudentId(), dto.getSchoolId(), dto.getAcademicYearId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Student leave stats fetched successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TEACHER (CLASS TEACHER) ENDPOINTS
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/getClassStudentLeaves")
    public ResponseEntity<Map<String, Object>> getClassStudentLeaves(@RequestBody StudentLeaveDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            List<Map<String, Object>> leaves = studentLeaveService.getClassStudentLeaves(
                    dto.getSchoolId(), dto.getStatusFilter());

            if (leaves.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No student leave requests found for your class", null));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("leaves", leaves);
            return ResponseEntity.ok(build(200, "SUCCESS", "Class student leaves fetched successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SCHOOL ENDPOINTS
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping("/approve")
    public ResponseEntity<Map<String, Object>> approve(@RequestBody StudentLeaveDTO dto) {
        try {
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            studentLeaveService.approveLeave(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Leave approved successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(build(409, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    @PostMapping("/reject")
    public ResponseEntity<Map<String, Object>> reject(@RequestBody StudentLeaveDTO dto) {
        try {
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getReviewedBy() == null || dto.getReviewedBy().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'reviewedBy' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            studentLeaveService.rejectLeave(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Leave rejected successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(build(409, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    @PostMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getAll(@RequestBody StudentLeaveDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            List<Map<String, Object>> leaves = studentLeaveService.getAll(dto);
            if (leaves.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No leave records found", null));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("leaves", leaves);
            return ResponseEntity.ok(build(200, "SUCCESS", "Leave records fetched successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    @PostMapping("/getPending")
    public ResponseEntity<Map<String, Object>> getPending(@RequestBody StudentLeaveDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            List<Map<String, Object>> leaves = studentLeaveService.getPending(dto.getSchoolId(), dto.getAcademicYearId());
            if (leaves.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No pending leave requests found", null));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("leaves", leaves);
            return ResponseEntity.ok(build(200, "SUCCESS", "Pending leaves fetched successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    @PostMapping("/getById")
    public ResponseEntity<Map<String, Object>> getById(@RequestBody StudentLeaveDTO dto) {
        try {
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            Map<String, Object> leave = studentLeaveService.getById(dto.getId(), dto.getSchoolId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Leave fetched successfully", leave));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    @PostMapping("/getByStudent")
    public ResponseEntity<Map<String, Object>> getByStudent(@RequestBody StudentLeaveDTO dto) {
        try {
            if (dto.getStudentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            List<Map<String, Object>> leaves = studentLeaveService.getByStudent(
                    dto.getStudentId(), dto.getSchoolId(), dto.getAcademicYearId());
            if (leaves.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No leave records found for this student", null));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("leaves", leaves);
            return ResponseEntity.ok(build(200, "SUCCESS", "Student leave records fetched successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    @PostMapping("/getSchoolStats")
    public ResponseEntity<Map<String, Object>> getSchoolStats(@RequestBody StudentLeaveDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            Map<String, Object> stats = studentLeaveService.getSchoolStats(dto.getSchoolId(), dto.getAcademicYearId());
            return ResponseEntity.ok(build(200, "SUCCESS", "School leave stats fetched successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }
}
