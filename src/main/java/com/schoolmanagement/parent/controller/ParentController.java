package com.schoolmanagement.parent.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.schoolmanagement.parent.dto.AcademicProgressDTO;
import com.schoolmanagement.parent.dto.StudentReportRequestDTO;
import com.schoolmanagement.parent.service.ParentService;

@RestController
@RequestMapping("/parent")
public class ParentController {

    private final ParentService parentService;

    public ParentController(ParentService parentService) {
        this.parentService = parentService;
    }

    private Map<String, Object> build(int code, String status, String message, Object data) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", code);
        r.put("status", status);
        r.put("message", message);
        r.put("data", data);
        return r;
    }

    // ===== Report Card =====

    @PostMapping("/report-card")
    public ResponseEntity<Map<String, Object>> getReportCard(@RequestBody StudentReportRequestDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            if (dto.getStudentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required", null));

            AcademicProgressDTO result = parentService.getReportCard(
                    dto.getSchoolId(), dto.getAcademicYearId(), dto.getStudentId());

            return ResponseEntity.ok(build(200, "SUCCESS", "Report card fetched successfully", result));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal server error", null));
        }
    }
}
