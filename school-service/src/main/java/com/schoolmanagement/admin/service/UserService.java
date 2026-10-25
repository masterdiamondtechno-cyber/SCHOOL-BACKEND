package com.schoolmanagement.admin.service;

import java.util.Map;
import com.schoolmanagement.admin.dto.AdminRegisterRequestDTO;
import com.schoolmanagement.admin.dto.AdminVerifyRequestDTO;
import com.schoolmanagement.owner.dto.OwnerRegisterRequestDTO;
import com.schoolmanagement.owner.dto.OwnerVerifyRequestDTO;
import com.schoolmanagement.school.dto.LoginRequestDTO;

public interface UserService {

    Map<String, Object> login(LoginRequestDTO request);

    Map<String, Object> logout(String token, Long userId, String email);

    // New Admin Registration & OTP Methods
    Map<String, Object> registerInitiate(AdminRegisterRequestDTO request);
    Map<String, Object> registerVerify(AdminVerifyRequestDTO request);
    Map<String, Object> loginInitiate(String username, String password);

    // New Owner Registration Methods
    Map<String, Object> registerOwnerInitiate(OwnerRegisterRequestDTO request);
    Map<String, Object> registerOwnerVerify(OwnerVerifyRequestDTO request);
}
