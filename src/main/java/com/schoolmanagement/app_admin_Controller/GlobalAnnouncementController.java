package com.schoolmanagement.app_admin_Controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.schoolmanagement.app_admin_DTO.GlobalAnnouncementDTO;
import com.schoolmanagement.app_admin_Service.GlobalAnnouncementService;

@RestController
@RequestMapping("/api/app_admin/announcements")
public class GlobalAnnouncementController {

    @Autowired
    private GlobalAnnouncementService service;

    private Map<String, Object> build(int code, String status, String message, Object data) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", code);
        r.put("status", status);
        r.put("message", message);
        r.put("data", data);
        return r;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createAnnouncement(@RequestBody GlobalAnnouncementDTO dto) {
        try {
            if (dto.getTitle() == null || dto.getTitle().isBlank()) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Subject is required", null));
            }
            if (dto.getMessage() == null || dto.getMessage().isBlank()) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Message content is required", null));
            }
            if (dto.getTargetAudience() == null || dto.getTargetAudience().isBlank()) {
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Target audience is required", null));
            }

            Object result = service.createAnnouncement(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Announcement broadcast successfully", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", e.getMessage(), null));
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getAllAnnouncements() {
        try {
            Object data = service.getAllAnnouncements();
            return ResponseEntity.ok(build(200, "SUCCESS", "All announcements retrieved successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", e.getMessage(), null));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteAnnouncement(@PathVariable Long id) {
        try {
            service.deleteAnnouncement(id);
            return ResponseEntity.ok(build(200, "SUCCESS", "Announcement deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", e.getMessage(), null));
        }
    }
}
