package com.schoolmanagement.app_admin_Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.schoolmanagement.app_admin_DTO.ApprovalRequestDTO;
import com.schoolmanagement.app_admin_DTO.SchoolStatusUpdateDTO;
import com.schoolmanagement.app_admin_Service.SchoolApprovalService;
import com.schoolmanagement.school_admin_Entity.School;

@RestController
@RequestMapping("/api/app_admin/SchoolApprovalController")
public class SchoolApprovalController {

    @Autowired
    private SchoolApprovalService approvalService;

    @PostMapping("/update-status")
    public ResponseEntity<?> approveOrReject(
            @RequestBody ApprovalRequestDTO request) {

        Map<String, Object> response =
                approvalService.approveOrReject(request);

        if ("SUCCESS".equals(response.get("status"))) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/update-school-status")
    public ResponseEntity<?> updateSchoolStatus(
            @RequestBody SchoolStatusUpdateDTO request) {

        Map<String, Object> response =
                approvalService.updateSchoolStatus(request);

        if ("SUCCESS".equals(response.get("status"))) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveSchools() {

        Map<String, Object> response = new HashMap<>();

        try {

            List<School> schools = approvalService.getActiveSchools();

            if (schools == null || schools.isEmpty()) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "No active schools found");
                response.put("data", null);

                return ResponseEntity.status(404).body(response);
            }

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "data fetched successfully");
            response.put("data", schools);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);

            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/inactive")
    public ResponseEntity<Map<String, Object>> getInactiveSchools() {

        Map<String, Object> response = new HashMap<>();

        try {

            List<School> schools = approvalService.getInactiveSchools();

            if (schools == null || schools.isEmpty()) {
                response.put("code", 404);
                response.put("status", "FAILED");
                response.put("message", "No inactive schools found");
                response.put("data", null);

                return ResponseEntity.status(404).body(response);
            }

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "data fetched successfully");
            response.put("data", schools);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);

            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/count/active")
    public ResponseEntity<Map<String, Object>> countActiveSchools() {

        Map<String, Object> response = new HashMap<>();

        try {

            long count = approvalService.countActiveSchools();

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "data fetched successfully");
            response.put("data", count);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);

            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/count/inactive")
    public ResponseEntity<Map<String, Object>> countInactiveSchools() {

        Map<String, Object> response = new HashMap<>();

        try {

            long count = approvalService.countInactiveSchools();

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "data fetched successfully");
            response.put("data", count);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put("code", 500);
            response.put("status", "FAILED");
            response.put("message", "Internal Server Error");
            response.put("data", null);

            return ResponseEntity.status(500).body(response);
        }
    }
}
