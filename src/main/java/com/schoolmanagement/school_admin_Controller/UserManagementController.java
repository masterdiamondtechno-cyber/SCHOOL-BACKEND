package com.schoolmanagement.school_admin_Controller;

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

import com.schoolmanagement.school_admin_DTO.UserManagementDTO;
import com.schoolmanagement.school_admin_Repo.AcademicYearRepository;
import com.schoolmanagement.school_admin_Repo.SchoolRepository;
import com.schoolmanagement.school_admin_Service.UserManagementService;

@RestController
@RequestMapping("/usermanagement")
public class UserManagementController {

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private AcademicYearRepository academicYearRepository;

    // ==================== Helper ====================
    private Map<String, Object> build(int code, String status, String message, Object data) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", code);
        r.put("status", status);
        r.put("message", message);
        r.put("data", data);
        return r;
    }

    // ==================== GET ALL USERS (teacher + student only) ====================
    @PostMapping("/getAllUsers")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestBody(required = false) Map<String, Long> request) {

        try {
            // 1. Body missing
            if (request == null || request.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "Request body is missing or empty", null));
            }

            Long schoolId = request.get("schoolId");
            Long academicYearId = request.get("academicYearId");

            // 2. Validate required fields
            if (schoolId == null) {
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "'schoolId' is required", null));
            }
            if (academicYearId == null) {
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "'academicYearId' is required", null));
            }

            // 3. Check existence
            if (!schoolRepository.existsById(schoolId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "School not found", null));
            }
            if (!academicYearRepository.existsById(academicYearId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "Academic Year not found", null));
            }

            // 4. Fetch unified list
            List<UserManagementDTO> users = userManagementService.getAllUsers(schoolId, academicYearId);

            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(build(404, "FAILED", "No users found for the given school and academic year", null));
            }

            return ResponseEntity.ok(
                    build(200, "SUCCESS", "Users fetched successfully", users));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Internal Server Error: " + e.getMessage(), null));
        }
    }
}
