package com.schoolmanagement.school_admin_Controller;

import java.util.HashMap;
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

import com.schoolmanagement.Config.JwtUtil;
import com.schoolmanagement.school_admin_DTO.LoginRequestDTO;
import com.schoolmanagement.school_admin_DTO.OwnerOtpVerifyRequestDTO;
import com.schoolmanagement.school_admin_Entity.RefreshToken;
import com.schoolmanagement.school_admin_Service.RefreshTokenService;
import com.schoolmanagement.school_admin_Service.UnifiedLoginService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UnifiedLoginService unifiedLoginService;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private JwtUtil jwtUtil;

    // ── Login ────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(unifiedLoginService.login(request));
    }

    @PostMapping("/otpverify")
    public ResponseEntity<?> otpVerify(@Valid @RequestBody OwnerOtpVerifyRequestDTO request) {
        return ResponseEntity.ok(unifiedLoginService.verifyOwnerOtp(request));
    }

    @PostMapping("/check-status")
    public ResponseEntity<?> checkStatus() {
        Map<String, String> res = new HashMap<>();
        res.put("status", "ACTIVE");
        return ResponseEntity.ok(res);
    }

    // ── Refresh ──────────────────────────────────────────────
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(
            @RequestBody Map<String, String> body) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            String refreshTokenStr = body.get("refreshToken");

            if (refreshTokenStr == null || refreshTokenStr.isBlank()) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "refreshToken is required");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            // Validate old refresh token
            RefreshToken oldToken = refreshTokenService.validateRefreshToken(refreshTokenStr);

            // Rebuild claims from refresh token data
            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put("uid", oldToken.getUserId());
            claims.put("role", oldToken.getUserType());
            if (oldToken.getSchoolId() != null) {
                claims.put("sid", oldToken.getSchoolId());
            }
            if (oldToken.getAcademicYearId() != null) {
                claims.put("ayi", oldToken.getAcademicYearId());
            }

            // Generate new access token
            String newAccessToken = jwtUtil.generateToken(oldToken.getEmail(), claims);

            // Rotate — revoke old, issue new refresh token
            RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(oldToken);

            // Build response data
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("accessToken", newAccessToken);
            data.put("refreshToken", newRefreshToken.getToken());
            data.put("tokenType", "Bearer");
            data.put("expiresIn", 3600);

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "Token refreshed successfully");
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", 401);
            response.put("status", "UNAUTHORIZED");
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // ── Logout ───────────────────────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> requestBody) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("code", 400);
                response.put("status", "FAILED");
                response.put("message", "Authorization header is missing or invalid");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            String token = authHeader.substring(7);
            return ResponseEntity.ok(unifiedLoginService.logout(token));

        } catch (Exception e) {
            response.put("code", 500);
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}