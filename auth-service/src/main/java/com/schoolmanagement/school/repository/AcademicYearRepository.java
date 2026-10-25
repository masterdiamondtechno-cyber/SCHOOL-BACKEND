package com.schoolmanagement.school.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school.entity.AcademicYear;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {

    List<AcademicYear> findBySchool_SchoolId(Long schoolId);

    Optional<AcademicYear> findByAcademicYearIdAndSchool_SchoolId(Long academicYearId, Long schoolId);

    Optional<AcademicYear> findBySchool_SchoolIdAndIsCurrentTrue(Long schoolId);

    boolean existsBySchool_SchoolIdAndYearName(Long schoolId, String yearName);

    void deleteBySchool_SchoolId(Long schoolId);

    // Find all academic years where endDate has passed and still active/current
    @Query("SELECT a FROM AcademicYear a WHERE a.endDate < :today AND (a.status = 'active' OR a.isCurrent = true)")
    List<AcademicYear> findExpiredActiveAcademicYears(@Param("today") LocalDate today);
}
