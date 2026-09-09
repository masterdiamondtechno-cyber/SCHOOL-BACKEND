package com.schoolmanagement.school_admin_Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "dashboard_item")
public class DashboardItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dashboard_item_id")
    private Long dashboardItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    @Column(name = "dashboard_item_name", nullable = false, length = 100)
    private String dashboardItemName;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getDashboardItemId() { return dashboardItemId; }
    public void setDashboardItemId(Long dashboardItemId) { this.dashboardItemId = dashboardItemId; }

    public School getSchool() { return school; }
    public void setSchool(School school) { this.school = school; }

    public AcademicYear getAcademicYear() { return academicYear; }
    public void setAcademicYear(AcademicYear academicYear) { this.academicYear = academicYear; }

    public String getDashboardItemName() { return dashboardItemName; }
    public void setDashboardItemName(String dashboardItemName) { this.dashboardItemName = dashboardItemName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public DashboardItem() { super(); }
}
