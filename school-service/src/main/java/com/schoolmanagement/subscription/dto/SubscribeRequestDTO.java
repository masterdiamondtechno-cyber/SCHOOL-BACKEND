package com.schoolmanagement.subscription.dto;

import com.schoolmanagement.subscription.entity.SchoolSubscription.BillingCycle;
import com.schoolmanagement.subscription.entity.SubscriptionPlan.PlanType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscribeRequestDTO {

    @NotNull(message = "Plan type is required")
    private PlanType planType;  // BASIC, PRO, ENTERPRISE

    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;  // MONTHLY, YEARLY
}
