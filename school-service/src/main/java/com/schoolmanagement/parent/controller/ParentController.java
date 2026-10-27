package com.schoolmanagement.parent.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.schoolmanagement.parent.dto.AcademicProgressDTO;
import com.schoolmanagement.parent.dto.ParentDashboardDTO;
import com.schoolmanagement.parent.dto.ParentTeacherMessageDTO;
import com.schoolmanagement.parent.dto.SendMessageRequestDTO;
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

    // ===== 1. Dashboard Overview =====

    @PostMapping("/dashboard-overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview(@RequestBody Map<String, Long> body) {
        try {
            Long schoolId = body.get("schoolId");
            Long studentId = body.get("studentId");
            Long academicYearId = body.get("academicYearId");

            if (schoolId == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (studentId == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required", null));
            if (academicYearId == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));

            ParentDashboardDTO result = parentService.getDashboardOverview(schoolId, studentId, academicYearId);

            return ResponseEntity.ok(build(200, "SUCCESS", "Dashboard overview fetched successfully", result));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal server error: " + e.getMessage(), null));
        }
    }

    // ===== 2. Report Card =====

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

    // ===== 3. Messages (Parent <-> Teacher) =====

    @PostMapping("/messages/send")
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody SendMessageRequestDTO request) {
        try {
            if (request.getSchoolId() == null || request.getStudentId() == null || request.getMessageText() == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "schoolId, studentId, and messageText are required", null));
            }
            ParentTeacherMessageDTO result = parentService.sendMessage(request);
            return ResponseEntity.ok(build(200, "SUCCESS", "Message sent successfully", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", e.getMessage(), null));
        }
    }

    @PostMapping("/messages/conversation")
    public ResponseEntity<Map<String, Object>> getConversation(@RequestBody Map<String, Long> body) {
        try {
            Long schoolId = body.get("schoolId");
            Long studentId = body.get("studentId");
            if (schoolId == null || studentId == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "schoolId and studentId are required", null));
            }
            List<ParentTeacherMessageDTO> list = parentService.getStudentConversation(schoolId, studentId);
            return ResponseEntity.ok(build(200, "SUCCESS", "Conversation fetched successfully", list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", e.getMessage(), null));
        }
    }

    @PostMapping("/messages/teacher-inbox")
    public ResponseEntity<Map<String, Object>> getTeacherMessages(@RequestBody Map<String, Long> body) {
        try {
            Long schoolId = body.get("schoolId");
            Long teacherId = body.get("teacherId");
            if (schoolId == null) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "schoolId is required", null));
            }
            List<ParentTeacherMessageDTO> list = parentService.getTeacherMessages(schoolId, teacherId);
            return ResponseEntity.ok(build(200, "SUCCESS", "Teacher messages fetched successfully", list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", e.getMessage(), null));
        }
    }
}
