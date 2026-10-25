package com.schoolmanagement.school.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school.entity.Attendance;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Per-lecture: find by school + batch + date + subject
    List<Attendance> findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndAttendanceDateAndAddSubject_SubjectId(
            Long schoolId, Long standardId, Long divisionId, LocalDate date, Long subjectId);

    // All subjects on a date for a batch
    List<Attendance> findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndAttendanceDate(
            Long schoolId, Long standardId, Long divisionId, LocalDate date);

    // Student history with date range
    List<Attendance> findBySchool_SchoolIdAndAddStudent_IdAndAttendanceDateBetween(
            Long schoolId, Long studentId, LocalDate from, LocalDate to);

    // Check if attendance already marked for a student+date+subject
    Optional<Attendance> findBySchool_SchoolIdAndAddStudent_IdAndAttendanceDateAndAddSubject_SubjectId(
            Long schoolId, Long studentId, LocalDate date, Long subjectId);

    // Summary counts
    long countBySchool_SchoolIdAndAttendanceDateAndStatus(Long schoolId, LocalDate date, String status);
    long countBySchool_SchoolIdAndAttendanceDate(Long schoolId, LocalDate date);

    // Count per subject
    long countBySchool_SchoolIdAndAddStudent_IdAndAddSubject_SubjectIdAndStatus(
            Long schoolId, Long studentId, Long subjectId, String status);
    long countBySchool_SchoolIdAndAddStudent_IdAndAddSubject_SubjectId(
            Long schoolId, Long studentId, Long subjectId);

    // Count overall for a student
    long countBySchool_SchoolIdAndAddStudent_IdAndStatus(Long schoolId, Long studentId, String status);
    long countBySchool_SchoolIdAndAddStudent_Id(Long schoolId, Long studentId);

    // ==================== ACADEMIC YEAR FILTERED QUERIES ====================

    // Per-lecture: find by school + standard + batch + date + subject + academicYear
    List<Attendance> findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndAttendanceDateAndAddSubject_SubjectIdAndAcademicYear_AcademicYearId(
            Long schoolId, Long standardId, Long divisionId, LocalDate date, Long subjectId, Long academicYearId);

    // All subjects on a date for a batch + academicYear
    List<Attendance> findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndAttendanceDateAndAcademicYear_AcademicYearId(
            Long schoolId, Long standardId, Long divisionId, LocalDate date, Long academicYearId);

    // Student history with date range + academicYear
    List<Attendance> findBySchool_SchoolIdAndAddStudent_IdAndAttendanceDateBetweenAndAcademicYear_AcademicYearId(
            Long schoolId, Long studentId, LocalDate from, LocalDate to, Long academicYearId);

    // All attendance history for a student + academicYear
    List<Attendance> findBySchool_SchoolIdAndAddStudent_IdAndAcademicYear_AcademicYearId(
            Long schoolId, Long studentId, Long academicYearId);

    // Summary counts + academicYear
    long countBySchool_SchoolIdAndAttendanceDateAndStatusAndAcademicYear_AcademicYearId(Long schoolId, LocalDate date, String status, Long academicYearId);
    long countBySchool_SchoolIdAndAcademicYear_AcademicYearId(Long schoolId, Long academicYearId);
    long countBySchool_SchoolIdAndAttendanceDateAndAcademicYear_AcademicYearId(Long schoolId, LocalDate date, Long academicYearId);

    List<Attendance> findBySchool_SchoolIdAndAddStandard_StandardIdAndAttendanceDateAndAcademicYear_AcademicYearId(
            Long schoolId, Long standardId, LocalDate date, Long academicYearId);

    void deleteBySchool_SchoolId(Long schoolId);
}
