package com.schoolmanagement.owner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerStudentDTO {

    private Long id;
    private String studentId;
    private String firstName;
    private String lastName;
    private String gender;
    private String status;
    private double totalFee;
    private double finalFee;
    private double amountPaid;
    private String feeStatus;   // PAID | PARTIAL | PENDING
    private Long schoolId;
    private String schoolName;
    private String standardName;
    private String divisionName;

}
