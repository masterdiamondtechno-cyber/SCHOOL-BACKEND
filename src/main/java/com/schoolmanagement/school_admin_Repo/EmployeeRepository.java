package com.schoolmanagement.school_admin_Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school_admin_Entity.Employee;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findBySchool_SchoolId(Long schoolId);

    List<Employee> findBySchool_SchoolIdAndAcademicYear_AcademicYearId(Long schoolId, Long academicYearId);

    Optional<Employee> findByIdAndSchool_SchoolId(Long id, Long schoolId);

    List<Employee> findBySchool_SchoolIdAndStatus(Long schoolId, String status);

    List<Employee> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(Long schoolId, Long academicYearId, String status);

    boolean existsBySchool_SchoolIdAndEmailId(Long schoolId, String emailId);

    boolean existsBySchool_SchoolIdAndMobileNumber(Long schoolId, String mobileNumber);

    long countBySchool_SchoolIdAndStatus(Long schoolId, String status);

    long countBySchool_SchoolId(Long schoolId);
}
