package com.schoolmanagement.owner.controller;

import com.schoolmanagement.owner.dto.OwnerRegisterRequestDTO;
import com.schoolmanagement.owner.dto.OwnerVerifyRequestDTO;
import com.schoolmanagement.app_admin_Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/owner")
public class OwnerRegisterController {

    @Autowired
    private UserService userService;

    @PostMapping("/register/initiate")
    public ResponseEntity<Map<String, Object>> registerInitiate(@Valid @RequestBody OwnerRegisterRequestDTO request) {
        return ResponseEntity.ok(userService.registerOwnerInitiate(request));
    }

    @PostMapping("/register/verify")
    public ResponseEntity<Map<String, Object>> registerVerify(@Valid @RequestBody OwnerVerifyRequestDTO request) {
        return ResponseEntity.ok(userService.registerOwnerVerify(request));
    }
}
