package com.schoolmanagement.school.entity;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "study_groups")
public class StudyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String groupName;

    @Column(length = 500)
    private String description;

    // ===== FK: created by teacher =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_teacher_id", nullable = false)
    private AddTeacher createdBy;

    // ===== FK: standard =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_id")
    private AddStandard addStandard;

    // ===== FK: school =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    // ===== FK: academic_year =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    // ===== FK: division =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id")
    private AddDivision addDivision;

    @Column(nullable = false)
    private Boolean isActive = true;

    // ==================== GETTERS & SETTERS ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public AddTeacher getCreatedBy() { return createdBy; }
    public void setCreatedBy(AddTeacher createdBy) { this.createdBy = createdBy; }

    public AddStandard getAddStandard() { return addStandard; }
    public void setAddStandard(AddStandard addStandard) { this.addStandard = addStandard; }

    public School getSchool() { return school; }
    public void setSchool(School school) { this.school = school; }

    public AcademicYear getAcademicYear() { return academicYear; }
    public void setAcademicYear(AcademicYear academicYear) { this.academicYear = academicYear; }

    public AddDivision getAddDivision() { return addDivision; }
    public void setAddDivision(AddDivision addDivision) { this.addDivision = addDivision; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    // ==================== AUDIT FIELDS ====================

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.createdTimestamp = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.updatedTimestamp == null) {
            this.updatedTimestamp = LocalDateTime.now();
        }
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(LocalDateTime createdTimestamp) { this.createdTimestamp = createdTimestamp; }

    public LocalDateTime getUpdatedTimestamp() { return updatedTimestamp; }
    public void setUpdatedTimestamp(LocalDateTime updatedTimestamp) { this.updatedTimestamp = updatedTimestamp; }

    public StudyGroup() { super(); }
}
