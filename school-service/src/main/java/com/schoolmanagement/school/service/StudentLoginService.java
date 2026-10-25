package com.schoolmanagement.school.service;

import java.util.Map;

import com.schoolmanagement.school.dto.LoginRequestDTO;

public interface StudentLoginService {

    Map<String, Object> login(LoginRequestDTO request);

    Map<String, Object> logout(String token, Long studentId, String email, String mobile);
}
