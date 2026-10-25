package com.schoolmanagement.owner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerSchoolDTO {

    private Long schoolId;
    private String schoolName;
    private String district;
    private String state;
    private String schoolStatus;
    private String subscriptionStatus;
    private String phoneNo;
    private String email;
    private String createdAt;
    private long studentCount;
    private long teacherCount;
    private double totalRevenue;
    private String registrationStatus;

}
