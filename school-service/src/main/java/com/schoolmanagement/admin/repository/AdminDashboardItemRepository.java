package com.schoolmanagement.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.admin.entity.AdminDashboardItem;

@Repository
public interface AdminDashboardItemRepository extends JpaRepository<AdminDashboardItem, Long> {

    List<AdminDashboardItem> findByRole(String role);

    boolean existsByDashboardItemNameAndRole(String dashboardItemName, String role);
}
