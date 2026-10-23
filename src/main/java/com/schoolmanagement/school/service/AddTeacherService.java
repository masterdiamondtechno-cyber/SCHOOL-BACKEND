package com.schoolmanagement.school.service;

import java.util.List;
import java.util.Map;

import com.schoolmanagement.school.dto.AddTeacherDTO;
import com.schoolmanagement.school.dto.ResetTeacherPasswordsBulkDTO;

public interface AddTeacherService {

    void createTeacher(AddTeacherDTO dto);
    List<AddTeacherDTO> getAllBySchoolId(Long schoolId, Long academicYearId);
    AddTeacherDTO getByIdAndSchoolId(Long id, Long schoolId);
    boolean deleteTeacher(Long id, Long schoolId);
    List<AddTeacherDTO> getTeachersByStatus(Long schoolId, Long academicYearId, String status);
    long countBySchoolIdAndStatus(Long schoolId, Long academicYearId, String status);
    long countBySchoolId(Long schoolId, Long academicYearId);
    boolean schoolExists(Long schoolId);
    boolean isMobileExists(Long schoolId, String mobileNumber);
    boolean isEmailExists(Long schoolId, String emailId);
    void changeTeacherStatus(Long id, Long schoolId, String status);
    List<Map<String, Object>> getTeacherIdAndNameBySchoolId(Long schoolId, Long academicYearId);
    void updateTeacher(AddTeacherDTO dto);
    int resetPasswordsBulk(ResetTeacherPasswordsBulkDTO dto);
}
