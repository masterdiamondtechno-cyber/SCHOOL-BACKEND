package com.schoolmanagement.subscription.dto;

import com.schoolmanagement.subscription.entity.SubscriptionPlan.PlanType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlanDTO {

    private Long id;
    private PlanType planType;
    private String displayName;
    private String description;
    private BigDecimal monthlyPrice;
    private BigDecimal yearlyPrice;
    private Integer maxStudents;
    private Integer maxTeachers;
    private Integer maxStaff;

    // Feature flags
    private Boolean smsNotifications;
    private Boolean videoLectures;
    private Boolean studyMaterials;
    private Boolean advancedReports;
    private Boolean customBranding;
    private Boolean prioritySupport;
    private Boolean apiAccess;

    // Yearly savings (monthly * 12 - yearly)
    private BigDecimal yearlySavings;
}
