package com.schoolmanagement.app_admin_Controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.schoolmanagement.app_admin_Service.UserService;
import com.schoolmanagement.school_admin_DTO.LoginRequestDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth/admin")
public class AppAdminAuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> adminLogout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> requestBody) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            if (authHeader == null || authHeader.trim().isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Authorization header is missing");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (!authHeader.startsWith("Bearer ")) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Invalid Authorization header format");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (requestBody == null || requestBody.isEmpty()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Request body is missing");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (!requestBody.containsKey("userId") ||
                !requestBody.containsKey("email")) {

                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "userId and email are required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            String token = authHeader.substring(7);
            Long userId = Long.valueOf(requestBody.get("userId").toString());
            String email = requestBody.get("email").toString();

            userService.logout(token, userId, email);

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "Admin Logout successful");
            response.put("data", null);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
