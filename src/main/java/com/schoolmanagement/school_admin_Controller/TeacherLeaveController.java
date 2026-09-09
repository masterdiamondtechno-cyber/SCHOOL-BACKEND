package com.schoolmanagement.school_admin_Controller;

import com.schoolmanagement.school_admin_DTO.TeacherLeaveDTO;
import com.schoolmanagement.school_admin_Repo.AcademicYearRepository;
import com.schoolmanagement.school_admin_Repo.SchoolRepository;
import com.schoolmanagement.school_admin_Service.TeacherLeaveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teacher-leave")
public class TeacherLeaveController {

    private final TeacherLeaveService teacherLeaveService;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;

    public TeacherLeaveController(TeacherLeaveService teacherLeaveService,
                                  SchoolRepository schoolRepository,
                                  AcademicYearRepository academicYearRepository) {
        this.teacherLeaveService   = teacherLeaveService;
        this.schoolRepository      = schoolRepository;
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
            List.of("SICK", "CASUAL", "EMERGENCY", "MATERNITY", "UNPAID", "OTHER");

    private static final List<String> VALID_STATUSES =
            List.of("PENDING", "APPROVED", "REJECTED", "CANCELLED");

    // ─────────────────────────────────────────────────────────────────────────
    // TEACHER ENDPOINTS
    // ─────────────────────────────────────────────────────────────────────────

    // POST /teacher-leave/apply
    // Teacher identified via JWT — no teacherId needed in body
    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> apply(@RequestBody TeacherLeaveDTO dto) {
        try {
            if (dto.getFromDate() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'fromDate' is required", null));
            if (dto.getToDate() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'toDate' is required", null));
            if (dto.getLeaveType() != null && !VALID_LEAVE_TYPES.contains(dto.getLeaveType().toUpperCase()))
                return ResponseEntity.badRequest().body(build(400, "FAILED",
                        "leaveType must be one of: SICK, CASUAL, EMERGENCY, MATERNITY, UNPAID, OTHER", null));
            if (dto.getAcademicYearId() != null && !academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic year not found", null));

            teacherLeaveService.applyLeave(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Leave applied successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(build(409, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // POST /teacher-leave/cancel
    // Teacher cancels their own PENDING leave — identified via JWT
    @PostMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancel(@RequestBody TeacherLeaveDTO dto) {
        try {
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            teacherLeaveService.cancelLeave(dto.getId(), dto.getSchoolId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Leave cancelled successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(build(409, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // POST /teacher-leave/getMyLeaves
    // Teacher views own leave history
    @PostMapping("/getMyLeaves")
    public ResponseEntity<Map<String, Object>> getMyLeaves(@RequestBody TeacherLeaveDTO dto) {
        try {
            if (dto.getTeacherId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'teacherId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            List<Map<String, Object>> leaves = teacherLeaveService.getMyLeaves(
                    dto.getTeacherId(), dto.getSchoolId(), dto.getStatusFilter());

            if (leaves.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No leave records found", null));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("leaves", leaves);
            return ResponseEntity.ok(build(200, "SUCCESS", "Leave history fetched successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // POST /teacher-leave/getMyStats
    // Teacher views own stats summary
    @PostMapping("/getMyStats")
    public ResponseEntity<Map<String, Object>> getMyStats(@RequestBody TeacherLeaveDTO dto) {
        try {
            if (dto.getTeacherId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'teacherId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            Map<String, Object> stats = teacherLeaveService.getMyStats(
                    dto.getTeacherId(), dto.getSchoolId(), dto.getAcademicYearId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Teacher leave stats fetched successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SCHOOL ENDPOINTS
    // ─────────────────────────────────────────────────────────────────────────

    // POST /teacher-leave/approve
    @PostMapping("/approve")
    public ResponseEntity<Map<String, Object>> approve(@RequestBody TeacherLeaveDTO dto) {
        try {
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            teacherLeaveService.approveLeave(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Leave approved successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(build(409, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // POST /teacher-leave/reject
    @PostMapping("/reject")
    public ResponseEntity<Map<String, Object>> reject(@RequestBody TeacherLeaveDTO dto) {
        try {
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getReviewedBy() == null || dto.getReviewedBy().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'reviewedBy' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            teacherLeaveService.rejectLeave(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Leave rejected successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(build(409, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // POST /teacher-leave/getAll  (with filters: status, leaveType, date range)
    @PostMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getAll(@RequestBody TeacherLeaveDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            List<Map<String, Object>> leaves = teacherLeaveService.getAll(dto);
            if (leaves.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No leave records found", null));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("leaves", leaves);
            return ResponseEntity.ok(build(200, "SUCCESS", "Leave records fetched successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // POST /teacher-leave/getPending
    @PostMapping("/getPending")
    public ResponseEntity<Map<String, Object>> getPending(@RequestBody TeacherLeaveDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            List<Map<String, Object>> leaves = teacherLeaveService.getPending(dto.getSchoolId(), dto.getAcademicYearId());
            if (leaves.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No pending leave requests found", null));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("leaves", leaves);
            return ResponseEntity.ok(build(200, "SUCCESS", "Pending leaves fetched successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // POST /teacher-leave/getById
    @PostMapping("/getById")
    public ResponseEntity<Map<String, Object>> getById(@RequestBody TeacherLeaveDTO dto) {
        try {
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            Map<String, Object> leave = teacherLeaveService.getById(dto.getId(), dto.getSchoolId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Leave fetched successfully", leave));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // POST /teacher-leave/getByTeacher
    @PostMapping("/getByTeacher")
    public ResponseEntity<Map<String, Object>> getByTeacher(@RequestBody TeacherLeaveDTO dto) {
        try {
            if (dto.getTeacherId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'teacherId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            List<Map<String, Object>> leaves = teacherLeaveService.getByTeacher(
                    dto.getTeacherId(), dto.getSchoolId(), dto.getAcademicYearId());
            if (leaves.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No leave records found for this teacher", null));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("leaves", leaves);
            return ResponseEntity.ok(build(200, "SUCCESS", "Teacher leave records fetched successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // POST /teacher-leave/getSchoolStats
    @PostMapping("/getSchoolStats")
    public ResponseEntity<Map<String, Object>> getSchoolStats(@RequestBody TeacherLeaveDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            Map<String, Object> stats = teacherLeaveService.getSchoolStats(dto.getSchoolId(), dto.getAcademicYearId());
            return ResponseEntity.ok(build(200, "SUCCESS", "School leave stats fetched successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }
}