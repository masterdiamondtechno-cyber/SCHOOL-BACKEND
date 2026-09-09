package com.schoolmanagement.school_admin_Service;

import java.util.List;

import com.schoolmanagement.school_admin_DTO.UserManagementDTO;

public interface UserManagementService {

    List<UserManagementDTO> getAllUsers(Long schoolId, Long academicYearId);
}
