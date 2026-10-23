package com.schoolmanagement.school.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_leave")
public class TeacherLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String leaveRefId; // e.g. LV-2025-001

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private AddTeacher teacher;

    // Improvement: substitute teacher assigned by school on approval
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "substitute_teacher_id")
    private AddTeacher substituteTeacher;

    @Column(nullable = false)
    private String leaveType; // SICK | CASUAL | EMERGENCY | MATERNITY | UNPAID | OTHER

    @Column(nullable = false)
    private LocalDate fromDate;

    @Column(nullable = false)
    private LocalDate toDate;

    private Integer totalDays; // auto-computed on save

    @Column(length = 1000)
    private String reason;

    @Column(nullable = false)
    private String status; // PENDING | APPROVED | REJECTED | CANCELLED

    private String reviewedBy;      // school admin name who acted
    private String reviewRemark;    // school's note on approve/reject
    private LocalDateTime reviewedAt;

    private Integer leavesTakenInYear;
    private Integer leaveDaysTakenInYear;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;

    @PrePersist
    public void onCreate() {
        this.createdAt        = LocalDateTime.now();
        this.updatedAt        = LocalDateTime.now();
        this.createdTimestamp = LocalDateTime.now();
        // auto-compute totalDays
        if (this.fromDate != null && this.toDate != null)
            this.totalDays = (int) (this.toDate.toEpochDay() - this.fromDate.toEpochDay() + 1);
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.updatedTimestamp == null)
            this.updatedTimestamp = LocalDateTime.now();
    }

    public TeacherLeave() { super(); }

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLeaveRefId() { return leaveRefId; }
    public void setLeaveRefId(String leaveRefId) { this.leaveRefId = leaveRefId; }

    public School getSchool() { return school; }
    public void setSchool(School school) { this.school = school; }

    public AcademicYear getAcademicYear() { return academicYear; }
    public void setAcademicYear(AcademicYear academicYear) { this.academicYear = academicYear; }

    public AddTeacher getTeacher() { return teacher; }
    public void setTeacher(AddTeacher teacher) { this.teacher = teacher; }

    public AddTeacher getSubstituteTeacher() { return substituteTeacher; }
    public void setSubstituteTeacher(AddTeacher substituteTeacher) { this.substituteTeacher = substituteTeacher; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public Integer getTotalDays() { return totalDays; }
    public void setTotalDays(Integer totalDays) { this.totalDays = totalDays; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getReviewRemark() { return reviewRemark; }
    public void setReviewRemark(String reviewRemark) { this.reviewRemark = reviewRemark; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public Integer getLeavesTakenInYear() { return leavesTakenInYear; }
    public void setLeavesTakenInYear(Integer leavesTakenInYear) { this.leavesTakenInYear = leavesTakenInYear; }

    public Integer getLeaveDaysTakenInYear() { return leaveDaysTakenInYear; }
    public void setLeaveDaysTakenInYear(Integer leaveDaysTakenInYear) { this.leaveDaysTakenInYear = leaveDaysTakenInYear; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getCreatedTimestamp() { return createdTimestamp; }
    public void setCreatedTimestamp(LocalDateTime createdTimestamp) { this.createdTimestamp = createdTimestamp; }

    public LocalDateTime getUpdatedTimestamp() { return updatedTimestamp; }
    public void setUpdatedTimestamp(LocalDateTime updatedTimestamp) { this.updatedTimestamp = updatedTimestamp; }
}