package com.schoolmanagement.school.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school.entity.AddStandard;

@Repository
public interface AddStandardRepository extends JpaRepository<AddStandard, Long> {

    List<AddStandard> findBySchool_SchoolId(Long schoolId);

    List<AddStandard> findBySchool_SchoolIdAndAcademicYear_AcademicYearId(Long schoolId, Long academicYearId);

    Optional<AddStandard> findByStandardId(Long standardId);

    boolean existsBySchool_SchoolIdAndStandard(Long schoolId, String standard);

    void deleteBySchool_SchoolId(Long schoolId);

    Optional<AddStandard> findBySchool_SchoolIdAndStandardAndAcademicYear_AcademicYearId(
            Long schoolId, String standard, Long academicYearId);
}
