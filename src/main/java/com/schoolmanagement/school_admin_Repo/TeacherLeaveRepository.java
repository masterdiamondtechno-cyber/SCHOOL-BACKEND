package com.schoolmanagement.school_admin_Repo;

import com.schoolmanagement.school_admin_Entity.TeacherLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherLeaveRepository extends JpaRepository<TeacherLeave, Long>, JpaSpecificationExecutor<TeacherLeave> {

    // ── Basic finders ──────────────────────────────────────────────────────────
    List<TeacherLeave> findBySchool_SchoolId(Long schoolId);

    List<TeacherLeave> findBySchool_SchoolIdAndAcademicYear_AcademicYearId(Long schoolId, Long academicYearId);

    List<TeacherLeave> findBySchool_SchoolIdAndStatus(Long schoolId, String status);

    List<TeacherLeave> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(Long schoolId, Long academicYearId, String status);

    List<TeacherLeave> findByTeacher_IdAndSchool_SchoolId(Long teacherId, Long schoolId);

    List<TeacherLeave> findByTeacher_IdAndSchool_SchoolIdAndAcademicYear_AcademicYearId(Long teacherId, Long schoolId, Long academicYearId);

    Optional<TeacherLeave> findByIdAndSchool_SchoolId(Long id, Long schoolId);

    Optional<TeacherLeave> findByLeaveRefId(String leaveRefId);

    // ── For unique leaveRefId generation ──────────────────────────────────────
    @Query("SELECT MAX(t.leaveRefId) FROM TeacherLeave t")
    String findMaxLeaveRefId();

    // ── Improvement: Overlap detection ────────────────────────────────────────
    // Check if teacher already has an APPROVED or PENDING leave overlapping the requested dates
    @Query("SELECT COUNT(t) > 0 FROM TeacherLeave t " +
           "WHERE t.teacher.id = :teacherId " +
           "AND t.status IN ('PENDING', 'APPROVED') " +
           "AND t.fromDate <= :toDate " +
           "AND t.toDate >= :fromDate")
    boolean existsOverlappingLeave(
            @Param("teacherId") Long teacherId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    // ── Improvement: Auto-expire scheduler ────────────────────────────────────
    // Find all PENDING leaves where fromDate has already passed
    @Query("SELECT t FROM TeacherLeave t WHERE t.status = 'PENDING' AND t.fromDate < :today")
    List<TeacherLeave> findStalePendingLeaves(@Param("today") LocalDate today);

    // ── Stats queries ─────────────────────────────────────────────────────────
    long countBySchool_SchoolIdAndStatus(Long schoolId, String status);

    long countBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(Long schoolId, Long academicYearId, String status);

    long countByTeacher_IdAndSchool_SchoolIdAndStatus(Long teacherId, Long schoolId, String status);

    // Sum of approved leave days for a teacher in an academic year
    @Query("SELECT COALESCE(SUM(t.totalDays), 0) FROM TeacherLeave t " +
           "WHERE t.teacher.id = :teacherId AND t.school.schoolId = :schoolId " +
           "AND t.status = 'APPROVED'")
    Integer sumApprovedDaysByTeacher(@Param("teacherId") Long teacherId, @Param("schoolId") Long schoolId);

    // Sum of approved leave days for a teacher in specific academic year
    @Query("SELECT COALESCE(SUM(t.totalDays), 0) FROM TeacherLeave t " +
           "WHERE t.teacher.id = :teacherId AND t.school.schoolId = :schoolId " +
           "AND t.academicYear.academicYearId = :academicYearId AND t.status = 'APPROVED'")
    Integer sumApprovedDaysByTeacherAndYear(
            @Param("teacherId") Long teacherId,
            @Param("schoolId") Long schoolId,
            @Param("academicYearId") Long academicYearId);

    // Count of approved leaves for a teacher in specific academic year
    @Query("SELECT COUNT(t) FROM TeacherLeave t " +
           "WHERE t.teacher.id = :teacherId AND t.school.schoolId = :schoolId " +
           "AND t.academicYear.academicYearId = :academicYearId AND t.status = 'APPROVED'")
    Long countApprovedLeavesByTeacherAndYear(
            @Param("teacherId") Long teacherId,
            @Param("schoolId") Long schoolId,
            @Param("academicYearId") Long academicYearId);

    // Count of approved leaves for a teacher
    @Query("SELECT COUNT(t) FROM TeacherLeave t " +
           "WHERE t.teacher.id = :teacherId AND t.school.schoolId = :schoolId " +
           "AND t.status = 'APPROVED'")
    Long countApprovedLeavesByTeacher(
            @Param("teacherId") Long teacherId,
            @Param("schoolId") Long schoolId);

    // School-wide total approved days
    @Query("SELECT COALESCE(SUM(t.totalDays), 0) FROM TeacherLeave t " +
           "WHERE t.school.schoolId = :schoolId AND t.status = 'APPROVED'")
    Integer sumApprovedDaysBySchool(@Param("schoolId") Long schoolId);

    // ── Improvement: Filter by date range ─────────────────────────────────────
    @Query("SELECT t FROM TeacherLeave t WHERE t.school.schoolId = :schoolId " +
           "AND t.fromDate >= :from AND t.toDate <= :to")
    List<TeacherLeave> findBySchoolAndDateRange(
            @Param("schoolId") Long schoolId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT t FROM TeacherLeave t WHERE t.school.schoolId = :schoolId " +
           "AND t.leaveType = :leaveType")
    List<TeacherLeave> findBySchoolAndLeaveType(
            @Param("schoolId") Long schoolId,
            @Param("leaveType") String leaveType);
}