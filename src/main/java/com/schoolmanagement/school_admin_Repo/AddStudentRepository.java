package com.schoolmanagement.school_admin_Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school_admin_Entity.AddStudent;

@Repository
public interface AddStudentRepository extends JpaRepository<AddStudent, Long> {

    Optional<AddStudent> findByIdAndSchool_SchoolId(Long id, Long schoolId);

    Optional<AddStudent> findTopBySchool_SchoolIdOrderByStudentIdDesc(Long schoolId);

    List<AddStudent> findBySchool_SchoolIdAndStatus(Long schoolId, String status);

    List<AddStudent> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(Long schoolId, Long academicYearId, String status);

    Long countBySchool_SchoolIdAndStatus(Long schoolId, String status);

    Long countBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(Long schoolId, Long academicYearId, String status);

    Long countBySchool_SchoolId(Long schoolId);

    Long countBySchool_SchoolIdAndAcademicYear_AcademicYearId(Long schoolId, Long academicYearId);

    List<AddStudent> findBySchool_SchoolId(Long schoolId);

    List<AddStudent> findBySchool_SchoolIdAndAcademicYear_AcademicYearId(Long schoolId, Long academicYearId);

    boolean existsByEmailIdAndSchool_SchoolId(String emailId, Long schoolId);

    boolean existsByMobileNumberAndSchool_SchoolId(String mobileNumber, Long schoolId);

    boolean existsBySchool_SchoolId(Long schoolId);

    Optional<AddStudent> findTopByOrderByIdDesc();

    List<AddStudent> findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionId(
            Long schoolId, Long standardId, Long divisionId);

    List<AddStudent> findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndAcademicYear_AcademicYearId(
            Long schoolId, Long standardId, Long divisionId, Long academicYearId);

    void deleteBySchool_SchoolId(Long schoolId);

    // ===== Login queries =====
    Optional<AddStudent> findByAadharNumber(String aadharNumber);

    Optional<AddStudent> findByEmailIdOrMobileNumber(String emailId, String mobileNumber);

    Optional<AddStudent> findByToken(String token);

    // ===== Parent login queries =====
    List<AddStudent> findByParentEmailIdOrParentMobileNumber(String parentEmailId, String parentMobileNumber);

    Optional<AddStudent> findFirstByParentEmailIdAndParentPasswordIsNotNull(String parentEmailId);

    Optional<AddStudent> findByParentToken(String parentToken);
}
