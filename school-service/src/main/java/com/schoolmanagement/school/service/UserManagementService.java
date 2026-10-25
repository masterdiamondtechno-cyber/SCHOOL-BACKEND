package com.schoolmanagement.school.service;

import java.util.List;

import com.schoolmanagement.school.dto.UserManagementDTO;

public interface UserManagementService {

    List<UserManagementDTO> getAllUsers(Long schoolId, Long academicYearId);
}
