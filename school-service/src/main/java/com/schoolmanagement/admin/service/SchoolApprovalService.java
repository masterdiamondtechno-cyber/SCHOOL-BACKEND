package com.schoolmanagement.admin.service;

import java.util.List;
import java.util.Map;

import com.schoolmanagement.admin.dto.ApprovalRequestDTO;
import com.schoolmanagement.admin.dto.SchoolStatusUpdateDTO;
import com.schoolmanagement.school.entity.School;

public interface SchoolApprovalService {
    Map<String, Object> approveOrReject(ApprovalRequestDTO request);
    
    Map<String, Object> updateSchoolStatus(SchoolStatusUpdateDTO request);
    
    //get all active and inactive schools
    List<School> getActiveSchools();
    List<School> getInactiveSchools();
    
    //count all active and inactive schools
    long countActiveSchools();
    long countInactiveSchools();
}
