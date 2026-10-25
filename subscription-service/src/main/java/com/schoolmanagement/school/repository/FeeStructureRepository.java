package com.schoolmanagement.school.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school.entity.FeeStructure;

@Repository
public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {

    List<FeeStructure> findBySchool_SchoolIdAndAddStandard_StandardId(Long schoolId, Long standardId);

    List<FeeStructure> findBySchool_SchoolIdAndAddStandard_StandardIdAndAcademicYear_AcademicYearId(Long schoolId, Long standardId, Long academicYearId);

    List<FeeStructure> findBySchool_SchoolId(Long schoolId);

    List<FeeStructure> findBySchool_SchoolIdAndAcademicYear_AcademicYearId(Long schoolId, Long academicYearId);

    Optional<FeeStructure> findByFeeStructureIdAndSchool_SchoolId(Long feeStructureId, Long schoolId);

    void deleteBySchool_SchoolId(Long schoolId);
}
