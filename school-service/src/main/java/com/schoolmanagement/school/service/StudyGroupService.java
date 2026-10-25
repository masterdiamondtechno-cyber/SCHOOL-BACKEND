package com.schoolmanagement.school.service;

import java.util.List;

import com.schoolmanagement.school.dto.StudyGroupDTO;
import com.schoolmanagement.school.dto.StudyGroupMemberDTO;

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
