package com.schoolmanagement.school_admin_Service;

import java.util.Map;
import com.schoolmanagement.school_admin_DTO.LoginRequestDTO;

public interface UnifiedLoginService {
    Map<String, Object> login(LoginRequestDTO request);
    Map<String, Object> verifyOwnerOtp(com.schoolmanagement.school_admin_DTO.OwnerOtpVerifyRequestDTO request);
    Map<String, Object> logout(String token);
}