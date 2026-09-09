package com.schoolmanagement.school_admin_Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "add_division")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddDivision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "division_id")
    private Long divisionId;

    // ===== FK: References school.school_id =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    // ===== FK: References add_standard.standard_id =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_id", nullable = false)
    private AddStandard addStandard;

    // ===== FK: References academic_year =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    @Column(name = "division_name", nullable = false)
    private String divisionName;

    @Column(name = "description")
    private String description;

    // ===== FK: References teacher (class teacher for this division) =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_teacher_id")
    private AddTeacher classTeacher;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;

}
