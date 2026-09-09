package com.schoolmanagement.school_admin_Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school_admin_Entity.AddTeacher;

@Repository
public interface AddTeacherRepository extends JpaRepository<AddTeacher, Long> {

    List<AddTeacher> findBySchool_SchoolId(Long schoolId);

    List<AddTeacher> findBySchool_SchoolIdAndAcademicYear_AcademicYearId(Long schoolId, Long academicYearId);

    Optional<AddTeacher> findByIdAndSchool_SchoolId(Long id, Long schoolId);

    List<AddTeacher> findBySchool_SchoolIdAndStatus(Long schoolId, String status);

    List<AddTeacher> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(Long schoolId, Long academicYearId, String status);

    long countBySchool_SchoolIdAndStatus(Long schoolId, String status);

    long countBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(Long schoolId, Long academicYearId, String status);

    boolean existsBySchool_SchoolId(Long schoolId);

    long countBySchool_SchoolId(Long schoolId);

    long countBySchool_SchoolIdAndAcademicYear_AcademicYearId(Long schoolId, Long academicYearId);

    boolean existsBySchool_SchoolIdAndMobileNumber(Long schoolId, String mobileNumber);

    boolean existsBySchool_SchoolIdAndEmailId(Long schoolId, String emailId);

    void deleteBySchool_SchoolId(Long schoolId);

    // ===== Login queries =====
    Optional<AddTeacher> findByEmailIdOrMobileNumber(String emailId, String mobileNumber);

    Optional<AddTeacher> findByToken(String token);
}
