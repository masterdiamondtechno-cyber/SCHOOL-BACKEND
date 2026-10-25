package com.schoolmanagement.school.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school.entity.FeePayment;

@Repository
public interface FeePaymentRepository extends JpaRepository<FeePayment, Long> {

    List<FeePayment> findBySchool_SchoolIdAndAddStudent_Id(Long schoolId, Long studentId);

    List<FeePayment> findBySchool_SchoolIdAndAddStudent_IdAndAcademicYear_AcademicYearId(Long schoolId, Long studentId, Long academicYearId);

    List<FeePayment> findBySchool_SchoolId(Long schoolId);

    List<FeePayment> findBySchool_SchoolIdAndAcademicYear_AcademicYearId(Long schoolId, Long academicYearId);

    Optional<FeePayment> findByPaymentIdAndSchool_SchoolId(Long paymentId, Long schoolId);

    Optional<FeePayment> findTopByOrderByPaymentIdDesc();

    @Query("SELECT SUM(f.amountPaid) FROM FeePayment f WHERE f.school.schoolId = :schoolId")
    Double sumAmountPaidBySchoolId(Long schoolId);

    @Query("SELECT SUM(f.amountPaid) FROM FeePayment f WHERE f.school.schoolId = :schoolId AND f.academicYear.academicYearId = :academicYearId")
    Double sumAmountPaidBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId);

    @Query("SELECT SUM(f.amountPaid) FROM FeePayment f")
    Double sumTotalAmountPaid();

    @Query("SELECT SUM(f.amountPaid) FROM FeePayment f WHERE f.paymentDate BETWEEN :startDate AND :endDate")
    Double sumAmountPaidBetweenDates(java.time.LocalDate startDate, java.time.LocalDate endDate);

    @Query("SELECT f.school.schoolName as name, SUM(f.amountPaid) as value FROM FeePayment f GROUP BY f.school.schoolName")
    List<Map<String, Object>> getRevenuePerBranch();

    void deleteBySchool_SchoolId(Long schoolId);
}
