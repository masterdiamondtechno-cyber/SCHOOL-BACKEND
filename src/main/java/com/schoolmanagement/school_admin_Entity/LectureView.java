package com.schoolmanagement.school_admin_Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Tracks each time a student views/streams a recorded lecture.
 */
@Entity
@Table(name = "lecture_views")
public class LectureView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private RecordedLecture lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private AddStudent student;

    private LocalDateTime viewedAt;

    @PrePersist
    public void onCreate() {
        this.viewedAt = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RecordedLecture getLecture() { return lecture; }
    public void setLecture(RecordedLecture lecture) { this.lecture = lecture; }

    public AddStudent getStudent() { return student; }
    public void setStudent(AddStudent student) { this.student = student; }

    public LocalDateTime getViewedAt() { return viewedAt; }
    public void setViewedAt(LocalDateTime viewedAt) { this.viewedAt = viewedAt; }
}
