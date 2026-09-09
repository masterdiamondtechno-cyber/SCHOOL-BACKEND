package com.schoolmanagement.school_admin_Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school_admin_Entity.Assignment;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findBySchool_SchoolIdAndStatus(Long schoolId, String status);

    List<Assignment> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(Long schoolId, Long academicYearId, String status);

    List<Assignment> findBySchool_SchoolIdAndAddStandard_StandardIdAndStatus(Long schoolId, Long standardId, String status);

    List<Assignment> findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndStatus(
            Long schoolId, Long standardId, Long divisionId, String status);

    List<Assignment> findBySchool_SchoolIdAndAddTeacher_IdAndStatus(Long schoolId, Long teacherId, String status);

    List<Assignment> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndStatus(
            Long schoolId, Long academicYearId, Long standardId, Long divisionId, String status);

    Long countBySchool_SchoolIdAndStatus(Long schoolId, String status);

    void deleteBySchool_SchoolId(Long schoolId);
}
