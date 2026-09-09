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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "exam_marks",
       uniqueConstraints = @UniqueConstraint(columnNames = {"exam_id", "student_id", "subject_id"}))
public class ExamMarks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_marks_id")
    private Long examMarksId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private AddStudent addStudent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private AddSubject addSubject;

    @Column(name = "marks_obtained")
    private Double marksObtained;

    @Column(name = "max_marks")
    private Double maxMarks;

    private String grade;

    private String remark;

    @Column(name = "is_absent")
    private Boolean isAbsent = false;

    // ==================== GETTERS & SETTERS ====================

    public Long getExamMarksId() { return examMarksId; }
    public void setExamMarksId(Long examMarksId) { this.examMarksId = examMarksId; }

    public School getSchool() { return school; }
    public void setSchool(School school) { this.school = school; }

    public AcademicYear getAcademicYear() { return academicYear; }
    public void setAcademicYear(AcademicYear academicYear) { this.academicYear = academicYear; }

    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }

    public AddStudent getAddStudent() { return addStudent; }
    public void setAddStudent(AddStudent addStudent) { this.addStudent = addStudent; }

    public AddSubject getAddSubject() { return addSubject; }
    public void setAddSubject(AddSubject addSubject) { this.addSubject = addSubject; }

    public Double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Double marksObtained) { this.marksObtained = marksObtained; }

    public Double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Double maxMarks) { this.maxMarks = maxMarks; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Boolean getIsAbsent() { return isAbsent; }
    public void setIsAbsent(Boolean isAbsent) { this.isAbsent = isAbsent; }

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

    public ExamMarks() { super(); }
}
