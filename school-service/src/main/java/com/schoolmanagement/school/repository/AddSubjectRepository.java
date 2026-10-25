package com.schoolmanagement.school.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school.entity.AddSubject;

@Repository
public interface AddSubjectRepository extends JpaRepository<AddSubject, Long> {

    List<AddSubject> findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionId(
            Long schoolId,
            Long standardId,
            Long divisionId
    );

    List<AddSubject> findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndAcademicYear_AcademicYearId(
            Long schoolId,
            Long standardId,
            Long divisionId,
            Long academicYearId
    );

    Optional<AddSubject> findBySubjectId(Long subjectId);

    boolean existsBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndSubjectName(
            Long schoolId,
            Long standardId,
            Long divisionId,
            String subjectName
    );

    List<AddSubject> findByAddStandard_StandardId(Long standardId);

    List<AddSubject> findByAddDivision_DivisionId(Long divisionId);

    List<AddSubject> findBySchool_SchoolIdAndAcademicYear_AcademicYearId(Long schoolId, Long academicYearId);

    List<AddSubject> findByAddTeacher_IdAndAcademicYear_AcademicYearId(Long teacherId, Long academicYearId);

    List<AddSubject> findByAddTeacher_IdAndSchool_SchoolId(Long teacherId, Long schoolId);

    void deleteBySchool_SchoolId(Long schoolId);
}
