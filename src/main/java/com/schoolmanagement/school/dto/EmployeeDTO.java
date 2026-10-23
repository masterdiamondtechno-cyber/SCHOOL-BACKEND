package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO {

    private Long id;
    private Long schoolId;
    private Long academicYearId;
    private Long roleId;
    private String employeeName;
    private String emailId;
    private String mobileNumber;
    private LocalDate joiningDate;
    private Double salary;
    private String education;
    private String gender;
    private String employeeType;
    private String status;
    private String address;
    private String profilePhoto;
    private String aadharCardNumber;
    private String roleName;
}