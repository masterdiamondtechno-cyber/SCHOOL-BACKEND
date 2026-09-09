package com.schoolmanagement.school_admin_Controller;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.schoolmanagement.school_admin_Service.ExcelService;

@RestController
@RequestMapping("/excel")
public class ExcelController {

    @Autowired
    private ExcelService excelService;

    // ── Download blank templates (direct file download) ───────

    @PostMapping("/template/teacher")
    public ResponseEntity<InputStreamResource> downloadTeacherTemplate() {
        ByteArrayInputStream stream = excelService.generateTeacherTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=teacher_template.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }

    @PostMapping("/template/student")
    public ResponseEntity<InputStreamResource> downloadStudentTemplate() {
        ByteArrayInputStream stream = excelService.generateStudentTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student_template.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(stream));
    }

    // ── Upload filled Excel files ─────────────────────────────

    @PostMapping("/upload/teacher")
    public ResponseEntity<Map<String, Object>> uploadTeachers(
            @RequestParam("file") MultipartFile file,
            @RequestParam("schoolId") Long schoolId,
            @RequestParam("academicYearId") Long academicYearId,
            @RequestParam(value = "standardId", required = false) Long standardId,
            @RequestParam(value = "divisionId", required = false) Long divisionId) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(buildResponse(400, "FAILED", "File is empty", null));
        }

        Map<String, Object> result = excelService.uploadTeachers(file, schoolId, academicYearId, standardId, divisionId);
        return ResponseEntity.ok(buildResponse(200, "SUCCESS", "Teacher upload processed", result));
    }

    @PostMapping("/upload/student")
    public ResponseEntity<Map<String, Object>> uploadStudents(
            @RequestParam("file") MultipartFile file,
            @RequestParam("schoolId") Long schoolId,
            @RequestParam("academicYearId") Long academicYearId,
            @RequestParam("standardId") Long standardId,
            @RequestParam("divisionId") Long divisionId) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(buildResponse(400, "FAILED", "File is empty", null));
        }

        Map<String, Object> result = excelService.uploadStudents(file, schoolId, academicYearId, standardId, divisionId);
        return ResponseEntity.ok(buildResponse(200, "SUCCESS", "Student upload processed", result));
    }

    // ── Helper ────────────────────────────────────────────────

    private Map<String, Object> buildResponse(int code, String status, String message, Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", code);
        response.put("status", status);
        response.put("message", message);
        response.put("data", data);
        return response;
    }
}
