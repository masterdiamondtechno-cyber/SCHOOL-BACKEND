package com.schoolmanagement.app_admin_Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.app_admin_Entity.AdminDashboardItem;

@Repository
public interface AdminDashboardItemRepository extends JpaRepository<AdminDashboardItem, Long> {

    List<AdminDashboardItem> findByRole(String role);

    boolean existsByDashboardItemNameAndRole(String dashboardItemName, String role);
}
