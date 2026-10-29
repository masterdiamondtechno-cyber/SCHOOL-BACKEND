package com.schoolmanagement.school.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.schoolmanagement.Config.JwtUtil;
import com.schoolmanagement.school.dto.LoginRequestDTO;
import com.schoolmanagement.school.entity.AddStudent;
import com.schoolmanagement.school.repository.AddStudentRepository;
import com.schoolmanagement.school.service.StudentLoginService;

@Service
public class StudentLoginServiceImpl implements StudentLoginService {

    @Autowired private AddStudentRepository studentRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    // ── Helper ───────────────────────────────────────────────
    private Map<String, Object> buildClaims(AddStudent student) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("uid",  student.getId());
        claims.put("sid",  student.getSchool().getSchoolId());
        claims.put("role", student.getRole() != null ? student.getRole().getName() : "STUDENT");
        if (student.getAcademicYear() != null) {
            claims.put("ayi", student.getAcademicYear().getAcademicYearId());
            claims.put("ay",  student.getAcademicYear().getYearName());
        }
        return claims;
    }

    @Override
    public Map<String, Object> login(LoginRequestDTO request) {
        Map<String, Object> response = new HashMap<>();

        AddStudent student = studentRepository
                .findByEmailIdOrMobileNumber(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid Credentials"));

        if (!"Active".equalsIgnoreCase(student.getStatus()))
            throw new RuntimeException("Student is Inactive");

        if (!passwordEncoder.matches(request.getPassword(), student.getPassword()))
            throw new RuntimeException("Invalid Password");

        String token = jwtUtil.generateToken(student.getEmailId(), buildClaims(student));

        student.setToken(token);
        student.setLoginAt(LocalDateTime.now());
        studentRepository.save(student);

        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id",    student.getId());
        user.put("name",  (student.getFirstName() != null ? student.getFirstName() : "") + " " + (student.getLastName() != null ? student.getLastName() : ""));
        user.put("email", student.getEmailId());
        user.put("phone", student.getMobileNumber());

        Map<String, Object> schoolInfo = new LinkedHashMap<>();
        schoolInfo.put("id",   student.getSchool().getSchoolId());
        schoolInfo.put("name", student.getSchool().getSchoolName());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token",  token);
        data.put("user",   user);
        data.put("role",   "student");
        data.put("school", schoolInfo);

        response.put("status",  "success");
        response.put("message", "Login successful");
        response.put("data", data);
        return response;
    }

    @Override
    public Map<String, Object> logout(String token, Long studentId, String email, String mobile) {
        Map<String, Object> response = new HashMap<>();

        AddStudent student = studentRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid Token"));

        if (!student.getId().equals(studentId) ||
            !student.getEmailId().equals(email) ||
            !student.getMobileNumber().equals(mobile))
            throw new RuntimeException("Student details do not match");

        student.setToken(null);
        student.setLogoutAt(LocalDateTime.now());
        studentRepository.save(student);

        response.put("code", 200);
        response.put("status", "SUCCESS");
        response.put("message", "Student Logged Out Successfully");
        response.put("data", null);
        return response;
    }
}