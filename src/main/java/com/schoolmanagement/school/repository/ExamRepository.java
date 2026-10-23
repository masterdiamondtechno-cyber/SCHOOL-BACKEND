package com.schoolmanagement.school.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school.entity.Exam;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findBySchool_SchoolId(Long schoolId);

    List<Exam> findBySchool_SchoolIdAndAcademicYear_AcademicYearId(Long schoolId, Long academicYearId);

    List<Exam> findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionId(
            Long schoolId, Long standardId, Long divisionId);

    List<Exam> findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndAcademicYear_AcademicYearId(
            Long schoolId, Long standardId, Long divisionId, Long academicYearId);

    Optional<Exam> findByExamIdAndSchool_SchoolId(Long examId, Long schoolId);

    List<Exam> findBySchool_SchoolIdAndStatus(Long schoolId, String status);

    List<Exam> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(Long schoolId, Long academicYearId, String status);

    List<Exam> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndAddStandard_StandardIdAndAddDivision_DivisionId(
            Long schoolId, Long academicYearId, Long standardId, Long divisionId);

    void deleteBySchool_SchoolId(Long schoolId);
}

