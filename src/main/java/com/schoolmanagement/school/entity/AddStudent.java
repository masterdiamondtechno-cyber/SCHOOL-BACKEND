package com.schoolmanagement.school.entity;

import jakarta.persistence.CascadeType;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "students")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String studentId;

    @Column(unique = true)
    private String prnNumber;

    private String rollNumber;

    // ===== FK: school =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    // ===== FK: academic_year =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    // ===== FK: standard =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_id")
    private AddStandard addStandard;

    // ===== FK: division =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id")
    private AddDivision addDivision;

    // ===== FK: role =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    // ===== Academic History Mapping =====
    @jakarta.persistence.OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StudentAcademicYear> academicHistory;

    // ===== Personal Info =====
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;

    // ===== Contact =====
    @Column(unique = true)
    private String mobileNumber;

    @Column(unique = true)
    private String emailId;

    // ===== Guardian =====
    private String guardianName;
    private String parentMobileNumber;
    private String parentEmailId;

    // ===== Academic =====
    private LocalDate admissionDate;

    // ===== Fee =====
    private String feeType;
    private Double totalFee;
    private Double discount;
    private Double finalFee;

    // ===== Login =====
    private String token;
    private LocalDateTime loginAt;
    private LocalDateTime logoutAt;

    // ===== Other =====
    private String address;
    private String profilePhoto;
    @Column(name = "aadhar_number")
    private String aadharNumber;
    private String status;
    private String password;

    private boolean passwordChangeRequired = true;

    private String parentPassword;
    private String parentToken;
    private LocalDateTime parentLoginAt;
    private LocalDateTime parentLogoutAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;
}
