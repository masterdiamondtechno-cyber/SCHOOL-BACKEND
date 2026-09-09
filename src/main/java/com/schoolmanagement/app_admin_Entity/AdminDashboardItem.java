package com.schoolmanagement.app_admin_Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_dashboard_item")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDashboardItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_dashboard_item_id")
    private Long adminDashboardItemId;

    @Column(name = "dashboard_item_name", nullable = false, length = 100)
    private String dashboardItemName;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    private LocalDateTime createdAt;

}
