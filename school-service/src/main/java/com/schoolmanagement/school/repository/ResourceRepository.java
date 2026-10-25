package com.schoolmanagement.school.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school.entity.Resource;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findBySchool_SchoolId(Long schoolId);

    List<Resource> findBySchool_SchoolIdAndAcademicYear_AcademicYearId(Long schoolId, Long academicYearId);

    List<Resource> findBySchool_SchoolIdAndAddStandard_StandardId(Long schoolId, Long standardId);

    List<Resource> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndAddStandard_StandardId(Long schoolId, Long academicYearId, Long standardId);

    List<Resource> findBySchool_SchoolIdAndAddSubject_SubjectId(Long schoolId, Long subjectId);

    List<Resource> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndAddSubject_SubjectId(Long schoolId, Long academicYearId, Long subjectId);

    List<Resource> findBySchool_SchoolIdAndAddStandard_StandardIdAndAddSubject_SubjectId(
            Long schoolId, Long standardId, Long subjectId);

    List<Resource> findBySchool_SchoolIdAndResourceType(Long schoolId, String resourceType);

    List<Resource> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndResourceType(Long schoolId, Long academicYearId, String resourceType);

    void deleteBySchool_SchoolId(Long schoolId);
}
