package com.schoolmanagement.app_admin_Controller;

import com.schoolmanagement.app_admin_DTO.AdminRegisterRequestDTO;
import com.schoolmanagement.app_admin_DTO.AdminVerifyRequestDTO;
import com.schoolmanagement.app_admin_Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/admin")
public class AdminAuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register/initiate")
    public ResponseEntity<Map<String, Object>> registerInitiate(@Valid @RequestBody AdminRegisterRequestDTO request) {
        return ResponseEntity.ok(userService.registerInitiate(request));
    }

    @PostMapping("/register/verify")
    public ResponseEntity<Map<String, Object>> registerVerify(@Valid @RequestBody AdminVerifyRequestDTO request) {
        return ResponseEntity.ok(userService.registerVerify(request));
    }
}
