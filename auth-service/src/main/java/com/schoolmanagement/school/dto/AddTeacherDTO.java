package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddTeacherDTO {

    private Long id;
    private Long schoolId;
    private Long academicYearId;
    private Long standardId;
    private Long divisionId;
    private String standardName;
    private String divisionName;

    private String teacherName;
    private String gender;
    private LocalDate dateOfBirth;
    private String mobileNumber;
    private String emailId;

    private String salaryType;
    private LocalDate joiningDate;
    private String annualSalary;
    private String monthlySalary;
    private String pfStatus;
    private String pfAccount;
    private String paidInHandSalary;

    private String address;
    private String profilePhoto;
    private String aadharCardNumber;
    private String status;
}
