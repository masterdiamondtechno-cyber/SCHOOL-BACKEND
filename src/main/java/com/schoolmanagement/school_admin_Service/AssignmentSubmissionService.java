package com.schoolmanagement.school_admin_Service;

import java.util.List;

import com.schoolmanagement.school_admin_DTO.AssignmentSubmissionDTO;

public interface AssignmentSubmissionService {

    void submitAssignment(AssignmentSubmissionDTO dto);
    void editSubmission(AssignmentSubmissionDTO dto);
    AssignmentSubmissionDTO getSubmissionById(Long id);
    void gradeSubmission(AssignmentSubmissionDTO dto);
    List<AssignmentSubmissionDTO> getSubmissionsByStudent(Long studentId, Long schoolId);
    List<AssignmentSubmissionDTO> getSubmissionsByAssignment(Long assignmentId, Long schoolId);
    boolean hasStudentSubmitted(Long studentId, Long assignmentId);
    void markAsSeen(Long studentId, Long assignmentId, Long schoolId);
}
