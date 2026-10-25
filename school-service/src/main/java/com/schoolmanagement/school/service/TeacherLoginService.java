package com.schoolmanagement.school.service;

import java.util.Map;

import com.schoolmanagement.school.dto.LoginRequestDTO;

public interface TeacherLoginService {

    Map<String, Object> login(LoginRequestDTO request);

    Map<String, Object> logout(String token, Long teacherId, String email, String mobile);
}
