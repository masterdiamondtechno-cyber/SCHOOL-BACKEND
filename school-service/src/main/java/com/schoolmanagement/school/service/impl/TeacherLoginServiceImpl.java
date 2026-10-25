package com.schoolmanagement.school.serviceImpl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.schoolmanagement.Config.JwtUtil;
import com.schoolmanagement.school.dto.LoginRequestDTO;
import com.schoolmanagement.school.entity.AddTeacher;
import com.schoolmanagement.school.repository.AddTeacherRepository;
import com.schoolmanagement.school.service.TeacherLoginService;

@Service
public class TeacherLoginServiceImpl implements TeacherLoginService {

    @Autowired private AddTeacherRepository teacherRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    // ── Helper ───────────────────────────────────────────────
    private Map<String, Object> buildClaims(AddTeacher teacher) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("uid",  teacher.getId());
        claims.put("sid",  teacher.getSchool().getSchoolId());
        claims.put("role", teacher.getRole() != null ? teacher.getRole().getName() : "TEACHER");
        if (teacher.getAcademicYear() != null) {
            claims.put("ayi", teacher.getAcademicYear().getAcademicYearId());
            claims.put("ay",  teacher.getAcademicYear().getYearName());
        }
        return claims;
    }

    @Override
    public Map<String, Object> login(LoginRequestDTO request) {
        Map<String, Object> response = new HashMap<>();

        AddTeacher teacher = teacherRepository
                .findByEmailIdOrMobileNumber(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid Credentials"));

        if (!"Active".equalsIgnoreCase(teacher.getStatus()))
            throw new RuntimeException("Teacher is Inactive");

        if (!passwordEncoder.matches(request.getPassword(), teacher.getPassword()))
            throw new RuntimeException("Invalid Password");

        String token = jwtUtil.generateToken(teacher.getEmailId(), buildClaims(teacher));

        teacher.setToken(token);
        teacher.setLoginAt(LocalDateTime.now());
        teacherRepository.save(teacher);

        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id",    teacher.getId());
        user.put("name",  teacher.getTeacherName());
        user.put("email", teacher.getEmailId());
        user.put("phone", teacher.getMobileNumber());

        Map<String, Object> schoolInfo = new LinkedHashMap<>();
        schoolInfo.put("id",   teacher.getSchool().getSchoolId());
        schoolInfo.put("name", teacher.getSchool().getSchoolName());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token",  token);
        data.put("user",   user);
        data.put("role",   "teacher");
        data.put("school", schoolInfo);

        response.put("status",  "success");
        response.put("message", "Login successful");
        response.put("data", data);
        return response;
    }

    @Override
    public Map<String, Object> logout(String token, Long teacherId, String email, String mobile) {
        Map<String, Object> response = new HashMap<>();

        AddTeacher teacher = teacherRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid Token"));

        if (!teacher.getId().equals(teacherId) ||
            !teacher.getEmailId().equals(email) ||
            !teacher.getMobileNumber().equals(mobile))
            throw new RuntimeException("Teacher details do not match");

        teacher.setToken(null);
        teacher.setLogoutAt(LocalDateTime.now());
        teacherRepository.save(teacher);

        response.put("code", 200);
        response.put("status", "SUCCESS");
        response.put("message", "Teacher Logged Out Successfully");
        response.put("data", null);
        return response;
    }
}