package com.schoolmanagement.school.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.schoolmanagement.school.dto.LoginRequestDTO;
import com.schoolmanagement.school.dto.SchoolDTO;
import com.schoolmanagement.school.dto.SendOtpRequestDTO;
import com.schoolmanagement.school.dto.UpdatePasswordRequestDTO;
import com.schoolmanagement.school.dto.VerifyOtpRequestDTO;
import com.schoolmanagement.school.entity.School;

public interface SchoolService {

    School createSchool(SchoolDTO dto, MultipartFile logoFile);
    void resendRegistrationEmail(Long schoolId);

    List<School> getAllSchools();

    School getBySchoolId(Long schoolId);

    School updateSchool(Long schoolId, SchoolDTO dto, MultipartFile logoFile);

    void deleteSchool(Long schoolId);
    
    Map<String, Object> login(LoginRequestDTO request);

    Map<String, Object> logout(String token, Long schoolId, String phoneNo, String email);
    

    String sendOtp(SendOtpRequestDTO request);

    String verifyOtp(VerifyOtpRequestDTO request);

    String updatePassword(UpdatePasswordRequestDTO request);
}
