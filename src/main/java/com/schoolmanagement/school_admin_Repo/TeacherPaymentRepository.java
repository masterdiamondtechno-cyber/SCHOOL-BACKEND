package com.schoolmanagement.school_admin_Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school_admin_Entity.TeacherPayment;

@Repository
public interface TeacherPaymentRepository extends JpaRepository<TeacherPayment, Long> {

    List<TeacherPayment> findBySchool_SchoolId(Long schoolId);

    List<TeacherPayment> findBySchool_SchoolIdAndAcademicYear_AcademicYearId(Long schoolId, Long academicYearId);

    List<TeacherPayment> findBySchool_SchoolIdAndPaymentMonthAndPaymentYear(Long schoolId, String month, Integer year);

    List<TeacherPayment> findBySchool_SchoolIdAndStatus(Long schoolId, String status);

    List<TeacherPayment> findByTeacher_IdAndSchool_SchoolId(Long teacherId, Long schoolId);

    boolean existsByTeacher_IdAndPaymentMonthAndPaymentYear(Long teacherId, String month, Integer year);

    Optional<TeacherPayment> findByTeacherPaymentIdAndSchool_SchoolId(Long id, Long schoolId);

    long countBySchool_SchoolIdAndStatus(Long schoolId, String status);

    @Query("SELECT MAX(t.receiptNumber) FROM TeacherPayment t WHERE t.school.schoolId = :schoolId")
    String findMaxReceiptNumber(@Param("schoolId") Long schoolId);
}
