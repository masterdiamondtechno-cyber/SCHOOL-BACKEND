package com.schoolmanagement.school.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school.entity.RecordedLecture;

@Repository
public interface RecordedLectureRepository extends JpaRepository<RecordedLecture, Long> {

    // ── Fetch all for a school ────────────────────────────────────────────────
    List<RecordedLecture> findBySchool_SchoolId(Long schoolId);

    List<RecordedLecture> findBySchool_SchoolIdAndAcademicYear_AcademicYearId(
            Long schoolId, Long academicYearId);

    // ── Fetch with status filter ──────────────────────────────────────────────
    List<RecordedLecture> findBySchool_SchoolIdAndStatus(Long schoolId, String status);

    List<RecordedLecture> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(
            Long schoolId, Long academicYearId, String status);

    // ── Fetch with subject filter ─────────────────────────────────────────────
    List<RecordedLecture> findBySchool_SchoolIdAndSubject_SubjectId(
            Long schoolId, Long subjectId);

    List<RecordedLecture> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndSubject_SubjectId(
            Long schoolId, Long academicYearId, Long subjectId);

    // ── Fetch with both status + subject filters ──────────────────────────────
    List<RecordedLecture> findBySchool_SchoolIdAndStatusAndSubject_SubjectId(
            Long schoolId, String status, Long subjectId);

    List<RecordedLecture> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatusAndSubject_SubjectId(
            Long schoolId, Long academicYearId, String status, Long subjectId);

    // ── Fetch by division (for student access — Published only) ───────────────
    List<RecordedLecture> findByDivision_DivisionIdAndStatus(Long divisionId, String status);

    List<RecordedLecture> findByDivision_DivisionIdAndAcademicYear_AcademicYearIdAndStatus(
            Long divisionId, Long academicYearId, String status);

    // ── Fetch by teacher (teacher's own lectures) ─────────────────────────────
    List<RecordedLecture> findByTeacher_IdAndSchool_SchoolId(Long teacherId, Long schoolId);

    // ── For lectureId auto-generation (get last inserted record) ──────────────
    Optional<RecordedLecture> findTopByOrderByIdDesc();

    // ── Counts for stats card ─────────────────────────────────────────────────
    long countBySchool_SchoolId(Long schoolId);

    long countBySchool_SchoolIdAndAcademicYear_AcademicYearId(Long schoolId, Long academicYearId);

    long countBySchool_SchoolIdAndStatus(Long schoolId, String status);

    long countBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(
            Long schoolId, Long academicYearId, String status);

    // ── Sum of all views for stats card ───────────────────────────────────────
    @Query("SELECT COALESCE(SUM(r.viewCount), 0) FROM RecordedLecture r " +
           "WHERE r.school.schoolId = :schoolId")
    Long sumViewsBySchoolId(@Param("schoolId") Long schoolId);

    @Query("SELECT COALESCE(SUM(r.viewCount), 0) FROM RecordedLecture r " +
           "WHERE r.school.schoolId = :schoolId " +
           "AND r.academicYear.academicYearId = :academicYearId")
    Long sumViewsBySchoolIdAndAcademicYearId(@Param("schoolId") Long schoolId,
                                              @Param("academicYearId") Long academicYearId);

    // ── Cleanup when school is deleted ────────────────────────────────────────
    void deleteBySchool_SchoolId(Long schoolId);
}
