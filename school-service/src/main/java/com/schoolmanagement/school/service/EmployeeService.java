package com.schoolmanagement.school.service;

import com.schoolmanagement.school.dto.EmployeeDTO;

import java.util.List;

public interface EmployeeService {

    void addEmployee(EmployeeDTO dto);
    void updateEmployee(EmployeeDTO dto);
    void deleteEmployee(Long id, Long schoolId);
    List<EmployeeDTO> getActive(Long schoolId, Long academicYearId);
    List<EmployeeDTO> getInactive(Long schoolId, Long academicYearId);
    List<EmployeeDTO> getAll(Long schoolId, Long academicYearId);
    EmployeeDTO getById(Long id, Long schoolId);
    long countActive(Long schoolId);
    long countInactive(Long schoolId);
    long countTotal(Long schoolId);
    boolean checkEmail(Long schoolId, String emailId);
    boolean checkMobile(Long schoolId, String mobileNumber);
    void changeStatus(Long id, Long schoolId, String status);
}