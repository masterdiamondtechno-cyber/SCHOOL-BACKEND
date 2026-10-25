package com.schoolmanagement.school.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school.entity.AddStudent;
import com.schoolmanagement.school.entity.StudentAcademicYear;

@Repository
public interface StudentAcademicYearRepository extends JpaRepository<StudentAcademicYear, Long> {
    
    List<StudentAcademicYear> findByStudent_Id(Long studentId);
    
    Optional<StudentAcademicYear> findByStudent_IdAndAcademicYear_AcademicYearId(Long studentId, Long academicYearId);
    
    List<StudentAcademicYear> findByAcademicYear_AcademicYearIdAndStandard_StandardIdAndDivision_DivisionId(
            Long academicYearId, Long standardId, Long divisionId);
            
    List<StudentAcademicYear> findByAcademicYear_AcademicYearId(Long academicYearId);

    // JPQL Custom Queries for Phase 2 decoupling
    @Query("SELECT m.student FROM StudentAcademicYear m WHERE m.student.school.schoolId = :schoolId AND m.academicYear.academicYearId = :academicYearId AND m.status = :status")
    List<AddStudent> findStudentsBySchoolAndYearAndStatus(
            @Param("schoolId") Long schoolId, 
            @Param("academicYearId") Long academicYearId, 
            @Param("status") String status);

    @Query("SELECT COUNT(m) FROM StudentAcademicYear m WHERE m.student.school.schoolId = :schoolId AND m.academicYear.academicYearId = :academicYearId AND m.status = :status")
    Long countStudentsBySchoolAndYearAndStatus(
            @Param("schoolId") Long schoolId, 
            @Param("academicYearId") Long academicYearId, 
            @Param("status") String status);

    @Query("SELECT COUNT(m) FROM StudentAcademicYear m WHERE m.student.school.schoolId = :schoolId AND m.academicYear.academicYearId = :academicYearId")
    Long countStudentsBySchoolAndYear(
            @Param("schoolId") Long schoolId, 
            @Param("academicYearId") Long academicYearId);

    @Query("SELECT m.student FROM StudentAcademicYear m WHERE m.student.school.schoolId = :schoolId AND m.academicYear.academicYearId = :academicYearId")
    List<AddStudent> findStudentsBySchoolAndYear(
            @Param("schoolId") Long schoolId, 
            @Param("academicYearId") Long academicYearId);

    @Query("SELECT m.student FROM StudentAcademicYear m WHERE m.student.school.schoolId = :schoolId AND m.standard.standardId = :standardId AND m.division.divisionId = :divisionId")
    List<AddStudent> findStudentsByDivision(
            @Param("schoolId") Long schoolId, 
            @Param("standardId") Long standardId, 
            @Param("divisionId") Long divisionId);
}
