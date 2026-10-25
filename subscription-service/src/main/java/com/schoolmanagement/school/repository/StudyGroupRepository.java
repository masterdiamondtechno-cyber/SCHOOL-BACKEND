package com.schoolmanagement.school.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school.entity.StudyGroup;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

    List<StudyGroup> findBySchool_SchoolIdAndIsActive(Long schoolId, Boolean isActive);

    List<StudyGroup> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndIsActive(Long schoolId, Long academicYearId, Boolean isActive);

    List<StudyGroup> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndAddStandard_StandardIdAndIsActive(
            Long schoolId, Long academicYearId, Long standardId, Boolean isActive);

    List<StudyGroup> findByCreatedBy_IdAndSchool_SchoolId(Long teacherId, Long schoolId);

    Long countBySchool_SchoolIdAndIsActive(Long schoolId, Boolean isActive);

    void deleteBySchool_SchoolId(Long schoolId);
}
