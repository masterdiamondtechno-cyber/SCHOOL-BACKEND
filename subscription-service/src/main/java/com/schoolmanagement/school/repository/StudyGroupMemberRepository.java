package com.schoolmanagement.school.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school.entity.StudyGroupMember;

@Repository
public interface StudyGroupMemberRepository extends JpaRepository<StudyGroupMember, Long> {

    List<StudyGroupMember> findByStudyGroup_Id(Long studyGroupId);

    List<StudyGroupMember> findByStudent_Id(Long studentId);

    List<StudyGroupMember> findByStudent_IdAndAcademicYear_AcademicYearId(Long studentId, Long academicYearId);

    boolean existsByStudyGroup_IdAndStudent_Id(Long studyGroupId, Long studentId);

    Optional<StudyGroupMember> findByStudyGroup_IdAndStudent_Id(Long studyGroupId, Long studentId);

    Long countByStudyGroup_Id(Long studyGroupId);

    void deleteByStudyGroup_Id(Long studyGroupId);
}
