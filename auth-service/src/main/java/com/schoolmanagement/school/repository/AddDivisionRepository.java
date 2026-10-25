package com.schoolmanagement.school.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school.entity.AddDivision;

@Repository
public interface AddDivisionRepository extends JpaRepository<AddDivision, Long> {

    List<AddDivision> findBySchool_SchoolId(Long schoolId);

    List<AddDivision> findBySchool_SchoolIdAndAcademicYear_AcademicYearId(Long schoolId, Long academicYearId);

    List<AddDivision> findByAddStandard_StandardId(Long standardId);

    List<AddDivision> findByAddStandard_StandardIdAndAcademicYear_AcademicYearId(Long standardId, Long academicYearId);

    Optional<AddDivision> findByDivisionId(Long divisionId);

    boolean existsByAddStandard_StandardIdAndDivisionName(Long standardId, String divisionName);

    void deleteBySchool_SchoolId(Long schoolId);

    Optional<AddDivision> findBySchool_SchoolIdAndDivisionNameAndAddStandard_StandardIdAndAcademicYear_AcademicYearId(
            Long schoolId, String divisionName, Long standardId, Long academicYearId);
}
