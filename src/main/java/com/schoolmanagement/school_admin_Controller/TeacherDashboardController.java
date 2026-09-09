package com.schoolmanagement.school_admin_Controller;

import java.time.LocalDate;
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
@RequestMapping("/teacher/dashboard")
public class TeacherDashboardController {

    private final DashboardService dashboardService;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;

    public TeacherDashboardController(DashboardService dashboardService,
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

    private ResponseEntity<Map<String, Object>> validateRequest(DashboardItemDTO dto) {
        if (dto.getSchoolId() == null)
            return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
        if (dto.getAcademicYearId() == null)
            return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
        if (dto.getTeacherId() == null)
            return ResponseEntity.badRequest().body(build(400, "FAILED", "'teacherId' is required", null));

        if (!schoolRepository.existsById(dto.getSchoolId()))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
        if (!academicYearRepository.existsById(dto.getAcademicYearId()))
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));

        return null;
    }

    @PostMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(@RequestBody DashboardItemDTO dto) {
        try {
            ResponseEntity<Map<String, Object>> errorResponse = validateRequest(dto);
            if (errorResponse != null) return errorResponse;

            Map<String, Object> serviceResult = dashboardService.getTeacherStats(dto.getSchoolId(), dto.getAcademicYearId(), dto.getTeacherId());

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("teacherUsername", serviceResult.get("teacherName"));
            data.put("stats", serviceResult.get("stats"));

            return ResponseEntity.ok(build(200, "SUCCESS", "Teacher stats fetched successfully", data));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", e.getMessage(), null));
        }
    }

    @PostMapping("/schedule")
    public ResponseEntity<Map<String, Object>> getSchedule(@RequestBody DashboardItemDTO dto) {
        try {
            ResponseEntity<Map<String, Object>> errorResponse = validateRequest(dto);
            if (errorResponse != null) return errorResponse;

            LocalDate targetDate = dto.getScheduleDate() != null ? dto.getScheduleDate() : LocalDate.now();

            List<Map<String, Object>> scheduleList = dashboardService.getTeacherSchedule(dto.getSchoolId(), dto.getAcademicYearId(), dto.getTeacherId(), targetDate);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("date", targetDate.toString());
            data.put("schedule", scheduleList);

            return ResponseEntity.ok(build(200, "SUCCESS", "Teacher schedule fetched successfully", data));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", e.getMessage(), null));
        }
    }

    @PostMapping("/grading-tasks")
    public ResponseEntity<Map<String, Object>> getGradingTasks(@RequestBody DashboardItemDTO dto) {
        try {
            ResponseEntity<Map<String, Object>> errorResponse = validateRequest(dto);
            if (errorResponse != null) return errorResponse;

            List<Map<String, Object>> tasks = dashboardService.getTeacherGradingTasks(dto.getSchoolId(), dto.getAcademicYearId(), dto.getTeacherId());

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("pendingTasksCount", tasks.size());
            data.put("tasks", tasks);

            return ResponseEntity.ok(build(200, "SUCCESS", "Teacher grading tasks fetched successfully", data));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", e.getMessage(), null));
        }
    }
}
