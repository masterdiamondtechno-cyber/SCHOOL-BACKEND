package com.schoolmanagement.school.service;

import java.util.List;

import com.schoolmanagement.school.dto.AssignmentDTO;

public interface AssignmentService {

    void createAssignment(AssignmentDTO dto);
    AssignmentDTO getAssignmentById(Long id, Long schoolId);
    void updateAssignment(AssignmentDTO dto);
    void deleteAssignment(Long id, Long schoolId);
    List<AssignmentDTO> getAssignmentsBySchoolAndStandardAndDivision(Long schoolId, Long academicYearId, Long standardId, Long divisionId);
    List<AssignmentDTO> getActiveAssignmentsBySchool(Long schoolId, Long academicYearId);
    List<AssignmentDTO> getAssignmentsByTeacher(Long schoolId, Long teacherId);
    void closeAssignment(Long id, Long schoolId);
}
