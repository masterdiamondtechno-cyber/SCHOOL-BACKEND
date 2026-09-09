package com.schoolmanagement.school_admin_Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school_admin_Entity.ExamMarks;

@Repository
public interface ExamMarksRepository extends JpaRepository<ExamMarks, Long> {

    List<ExamMarks> findByExam_ExamIdAndSchool_SchoolId(Long examId, Long schoolId);

    List<ExamMarks> findByExam_ExamIdAndSchool_SchoolIdAndAcademicYear_AcademicYearId(Long examId, Long schoolId, Long academicYearId);

    List<ExamMarks> findByExam_ExamIdAndAddSubject_SubjectId(Long examId, Long subjectId);

    List<ExamMarks> findByAddStudent_IdAndSchool_SchoolId(Long studentId, Long schoolId);

    List<ExamMarks> findByAddStudent_IdAndSchool_SchoolIdAndAcademicYear_AcademicYearId(Long studentId, Long schoolId, Long academicYearId);

    List<ExamMarks> findByExam_ExamIdAndAddStudent_IdAndSchool_SchoolIdAndAcademicYear_AcademicYearId(Long examId, Long studentId, Long schoolId, Long academicYearId);

    Optional<ExamMarks> findByExam_ExamIdAndAddStudent_IdAndAddSubject_SubjectId(
            Long examId, Long studentId, Long subjectId);

    void deleteBySchool_SchoolId(Long schoolId);

    void deleteByExam_ExamId(Long examId);
}
