package com.schoolmanagement.owner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerTeacherDTO {

    private Long teacherId;
    private String teacherName;
    private String emailId;
    private String mobileNumber;
    private String status;
    private String monthlySalary;
    private String joiningDate;
    private String gender;
    private Long schoolId;
    private String schoolName;

}
