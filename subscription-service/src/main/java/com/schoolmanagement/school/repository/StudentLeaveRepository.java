package com.schoolmanagement.school.repository;

import com.schoolmanagement.school.entity.StudentLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentLeaveRepository extends JpaRepository<StudentLeave, Long>, JpaSpecificationExecutor<StudentLeave> {

    List<StudentLeave> findBySchool_SchoolId(Long schoolId);

    List<StudentLeave> findBySchool_SchoolIdAndAcademicYear_AcademicYearId(Long schoolId, Long academicYearId);

    List<StudentLeave> findBySchool_SchoolIdAndStatus(Long schoolId, String status);

    List<StudentLeave> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(Long schoolId, Long academicYearId, String status);

    List<StudentLeave> findByStudent_IdAndSchool_SchoolId(Long studentId, Long schoolId);

    List<StudentLeave> findByStudent_IdAndSchool_SchoolIdAndAcademicYear_AcademicYearId(Long studentId, Long schoolId, Long academicYearId);

    Optional<StudentLeave> findByIdAndSchool_SchoolId(Long id, Long schoolId);

    Optional<StudentLeave> findByLeaveRefId(String leaveRefId);

    @Query("SELECT MAX(s.leaveRefId) FROM StudentLeave s")
    String findMaxLeaveRefId();

    @Query("SELECT COUNT(s) > 0 FROM StudentLeave s " +
           "WHERE s.student.id = :studentId " +
           "AND s.status IN ('PENDING', 'APPROVED') " +
           "AND s.fromDate <= :toDate " +
           "AND s.toDate >= :fromDate")
    boolean existsOverlappingLeave(
            @Param("studentId") Long studentId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query("SELECT s FROM StudentLeave s WHERE s.status = 'PENDING' AND s.fromDate < :today")
    List<StudentLeave> findStalePendingLeaves(@Param("today") LocalDate today);

    long countBySchool_SchoolIdAndStatus(Long schoolId, String status);

    long countBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(Long schoolId, Long academicYearId, String status);

    long countByStudent_IdAndSchool_SchoolIdAndStatus(Long studentId, Long schoolId, String status);

    @Query("SELECT COALESCE(SUM(s.totalDays), 0) FROM StudentLeave s " +
           "WHERE s.student.id = :studentId AND s.school.schoolId = :schoolId " +
           "AND s.status = 'APPROVED'")
    Integer sumApprovedDaysByStudent(@Param("studentId") Long studentId, @Param("schoolId") Long schoolId);

    @Query("SELECT COALESCE(SUM(s.totalDays), 0) FROM StudentLeave s " +
           "WHERE s.student.id = :studentId AND s.school.schoolId = :schoolId " +
           "AND s.academicYear.academicYearId = :academicYearId AND s.status = 'APPROVED'")
    Integer sumApprovedDaysByStudentAndYear(
            @Param("studentId") Long studentId,
            @Param("schoolId") Long schoolId,
            @Param("academicYearId") Long academicYearId);

    @Query("SELECT COALESCE(SUM(s.totalDays), 0) FROM StudentLeave s " +
           "WHERE s.school.schoolId = :schoolId AND s.status = 'APPROVED'")
    Integer sumApprovedDaysBySchool(@Param("schoolId") Long schoolId);

    @Query("SELECT s FROM StudentLeave s WHERE s.school.schoolId = :schoolId " +
           "AND s.fromDate >= :from AND s.toDate <= :to")
    List<StudentLeave> findBySchoolAndDateRange(
            @Param("schoolId") Long schoolId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT s FROM StudentLeave s WHERE s.school.schoolId = :schoolId " +
           "AND s.leaveType = :leaveType")
    List<StudentLeave> findBySchoolAndLeaveType(
            @Param("schoolId") Long schoolId,
            @Param("leaveType") String leaveType);

    @Query("SELECT s FROM StudentLeave s WHERE s.student.addStandard.classTeacher.id = :teacherId " +
           "AND s.school.schoolId = :schoolId")
    List<StudentLeave> findByClassTeacherAndSchool(
            @Param("teacherId") Long teacherId,
            @Param("schoolId") Long schoolId);
}
