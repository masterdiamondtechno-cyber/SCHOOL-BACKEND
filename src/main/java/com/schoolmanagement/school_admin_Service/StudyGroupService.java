package com.schoolmanagement.school_admin_Service;

import java.util.List;

import com.schoolmanagement.school_admin_DTO.StudyGroupDTO;
import com.schoolmanagement.school_admin_DTO.StudyGroupMemberDTO;

public interface StudyGroupService {

    void createStudyGroup(StudyGroupDTO dto);
    StudyGroupDTO getStudyGroupById(Long id);
    void updateStudyGroup(StudyGroupDTO dto);
    void deactivateStudyGroup(Long id);
    List<StudyGroupDTO> getActiveGroupsBySchool(Long schoolId, Long academicYearId);
    List<StudyGroupDTO> getGroupsByStandard(Long schoolId, Long academicYearId, Long standardId);
    void addStudentToGroup(StudyGroupMemberDTO dto);
    void addStudentsToGroup(StudyGroupMemberDTO dto);
    void removeStudentFromGroup(Long studyGroupId, Long studentId);
    List<StudyGroupMemberDTO> getGroupMembers(Long studyGroupId);
    List<StudyGroupDTO> getGroupsByStudent(Long studentId, Long academicYearId);
}
