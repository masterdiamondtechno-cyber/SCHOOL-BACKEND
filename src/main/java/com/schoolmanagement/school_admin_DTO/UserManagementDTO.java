package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserManagementDTO {

    // ===== Common =====
    private Long id;
    private String userType;       // "teacher" or "student"
    private String name;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String gender;
    private LocalDate dateOfBirth;
    private String address;
    private String profilePhoto;
    private LocalDateTime createdAt;

    // ===== School / Academic context =====
    private Long schoolId;
    private Long academicYearId;
    private Long standardId;
    private Long divisionId;

    // ===== Teacher-specific =====
    private String salaryType;
    private LocalDate joiningDate;
    private String annualSalary;
    private String monthlySalary;
    private String pfStatus;
    private String pfAccount;
    private String paidInHandSalary;
    private String aadharCardNumber;

    // ===== Student-specific =====
    private String studentId;
    private String prnNumber;
    private String rollNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String guardianName;
    private String parentMobileNumber;
    private String parentEmailId;
    private LocalDate admissionDate;
    private String feeType;
    private Double totalFee;
    private Double discount;
    private Double finalFee;
    private String aadharNumber;

}
