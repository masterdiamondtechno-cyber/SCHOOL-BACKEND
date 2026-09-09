package com.schoolmanagement.app_admin_Service;

import java.util.List;
import java.util.Map;

import com.schoolmanagement.app_admin_DTO.ApprovalRequestDTO;
import com.schoolmanagement.app_admin_DTO.SchoolStatusUpdateDTO;
import com.schoolmanagement.school_admin_Entity.School;

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
