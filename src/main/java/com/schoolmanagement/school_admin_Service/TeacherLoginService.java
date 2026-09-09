package com.schoolmanagement.school_admin_Service;

import java.util.Map;

import com.schoolmanagement.school_admin_DTO.LoginRequestDTO;

public interface TeacherLoginService {

    Map<String, Object> login(LoginRequestDTO request);

    Map<String, Object> logout(String token, Long teacherId, String email, String mobile);
}
