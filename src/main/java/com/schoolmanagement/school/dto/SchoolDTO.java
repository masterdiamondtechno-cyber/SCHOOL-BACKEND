package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchoolDTO {

    private Long schoolId;
    private String schoolName;
    private String udiseNo;
    private String state;
    private String district;
    private String taluka;
    private String pincode;
    private String schoolAddress;

    private String email;
    private String phoneNo;
    private Long roleId;
    private String subscriptionStatus;
    private String schoolStatus;
    private Long ownerId;
    private String principalName;

}
