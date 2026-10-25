package com.schoolmanagement.subscription.dto;

import com.schoolmanagement.subscription.entity.SchoolSubscription.BillingCycle;
import com.schoolmanagement.subscription.entity.SchoolSubscription.SubscriptionStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolSubscriptionDTO {

    private Long id;
    private Long schoolId;
    private SubscriptionPlanDTO plan;
    private BillingCycle billingCycle;
    private SubscriptionStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate trialEndDate;
    private Boolean isTrialPeriod;
    private LocalDate nextRenewalDate;

    // Kitne din bacha hai subscription mein
    private Long daysRemaining;

    // Subscription active hai ya nahi
    private Boolean isActive;

    private LocalDateTime createdAt;
}
