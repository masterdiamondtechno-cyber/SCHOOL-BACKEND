package com.schoolmanagement.school_admin_Service;

import java.util.Map;

import com.schoolmanagement.school_admin_DTO.LoginRequestDTO;

public interface StudentLoginService {

    Map<String, Object> login(LoginRequestDTO request);

    Map<String, Object> logout(String token, Long studentId, String email, String mobile);
}
