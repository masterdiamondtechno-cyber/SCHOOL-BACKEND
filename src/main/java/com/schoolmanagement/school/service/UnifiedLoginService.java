package com.schoolmanagement.school.service;

import java.util.Map;
import com.schoolmanagement.school.dto.LoginRequestDTO;

public interface UnifiedLoginService {
    Map<String, Object> login(LoginRequestDTO request);
    Map<String, Object> verifyOwnerOtp(com.schoolmanagement.school.dto.OwnerOtpVerifyRequestDTO request);
    Map<String, Object> logout(String token);
}