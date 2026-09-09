package com.schoolmanagement.school_admin_Controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.schoolmanagement.school_admin_DTO.DashboardItemDTO;
import com.schoolmanagement.school_admin_Repo.AcademicYearRepository;
import com.schoolmanagement.school_admin_Repo.SchoolRepository;
import com.schoolmanagement.school_admin_Service.DashboardService;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;

    public DashboardController(DashboardService dashboardService,
                               SchoolRepository schoolRepository,
                               AcademicYearRepository academicYearRepository) {
        this.dashboardService = dashboardService;
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

    // ===== Summary =====

    @PostMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(@RequestBody DashboardItemDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            if (dto.getRole() == null || dto.getRole().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'role' is required", null));

            String role = dto.getRole().toLowerCase();
            if (role.equals("admin")) role = "school";
            if (!role.equals("school") && !role.equals("teacher") && !role.equals("student") && !role.equals("parent"))
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "Invalid role. Must be school, teacher, student, or parent", null));

            if ((role.equals("student") || role.equals("parent")) && dto.getStudentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required for this role", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "Academic Year not found", null));

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("role", role);
            summary.put("schoolId", dto.getSchoolId());
            summary.put("academicYearId", dto.getAcademicYearId());
            summary.put("message", "Summary endpoint ready");

            return ResponseEntity.ok(build(200, "SUCCESS", "Dashboard summary fetched", summary));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", e.getMessage(), null));
        }
    }

    // ===== Save Dashboard Item =====

    @PostMapping("/item/save")
    public ResponseEntity<Map<String, Object>> saveItem(@RequestBody DashboardItemDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            if (dto.getDashboardItemName() == null || dto.getDashboardItemName().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'dashboardItemName' is required", null));
            if (dto.getRole() == null || dto.getRole().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'role' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "Academic Year not found", null));

            String role = dto.getRole().toLowerCase();
            if (role.equals("admin")) role = "school";
            if (!role.equals("school") && !role.equals("teacher") && !role.equals("student") && !role.equals("parent"))
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "Invalid role. Must be school, teacher, student, or parent", null));

            dashboardService.saveItem(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Dashboard item saved successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(build(409, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ===== Get Dashboard Data by Role =====

    @PostMapping("/data")
    public ResponseEntity<Map<String, Object>> getDashboardData(@RequestBody DashboardItemDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            if (dto.getRole() == null || dto.getRole().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'role' is required", null));

            String role = dto.getRole().toLowerCase();
            if (role.equals("admin")) role = "school";
            if (!role.equals("school") && !role.equals("teacher") && !role.equals("student") && !role.equals("parent"))
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "Invalid role. Must be school, teacher, student, or parent", null));

            if ((role.equals("student") || role.equals("parent")) && dto.getStudentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required for this role", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "Academic Year not found", null));

            List<Map<String, Object>> items = dashboardService.getDashboardData(dto.getSchoolId(),
                    dto.getAcademicYearId(), role, dto.getTeacherId(), dto.getStudentId());

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("items", items);

            if (dto.getStudentId() != null && ("student".equalsIgnoreCase(role) || "parent".equalsIgnoreCase(role))) {
                try {
                    Map<String, Object> studentInfo = dashboardService.getStudentInfo(dto.getSchoolId(),
                            dto.getAcademicYearId(), dto.getStudentId());
                    data.put("studentInfo", studentInfo);
                } catch (Exception e) {
                    // Fallback if student details cannot be resolved
                }
            }

            return ResponseEntity.ok(build(200, "SUCCESS", "Dashboard data fetched successfully", data));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", e.getMessage(), null));
        }
    }
}
