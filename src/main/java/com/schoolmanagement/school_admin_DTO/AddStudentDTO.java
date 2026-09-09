package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddStudentDTO {

    private Long id;
    private Long schoolId;
    private Long academicYearId;
    private Long standardId;
    private Long divisionId;

    private String prnNumber;
    private String rollNumber;

    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;

    private String mobileNumber;
    private String emailId;

    private String guardianName;
    private String parentMobileNumber;
    private String parentEmailId;

    private LocalDate admissionDate;

    private String feeType;
    private Double totalFee;
    private Double discount;
    private Double finalFee;

    private String address;
    private String status;
    private String profilePhoto;
    private String aadharNumber;
    private boolean passwordChangeRequired;
    private String standard;
    private String divisionName;
}
