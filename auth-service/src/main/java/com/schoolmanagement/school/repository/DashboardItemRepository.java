package com.schoolmanagement.school.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school.entity.DashboardItem;

@Repository
public interface DashboardItemRepository extends JpaRepository<DashboardItem, Long> {

    List<DashboardItem> findByRole(String role);

    List<DashboardItem> findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndRole(Long schoolId, Long academicYearId, String role);

    boolean existsByDashboardItemNameAndRoleAndSchool_SchoolIdAndAcademicYear_AcademicYearId(String dashboardItemName, String role, Long schoolId, Long academicYearId);

    boolean existsByDashboardItemNameAndRole(String dashboardItemName, String role);

    void deleteBySchool_SchoolId(Long schoolId);
}
