package com.schoolmanagement.app_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionPlanDTO {

    private Long planId;
    private String planName;
    private Integer price;
    private String billingCycle;
    private Integer maxStudents;
    private Integer maxTeachers;
    private Integer storageLimitGb;
    private String features;
    private List<String> featureList; // Support for sending/receiving as a list
    private String supportLevel;
    private Boolean isMostPopular;
    private String status;
    private Integer discountPercentage;
    private String paymentLink;
}
