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
public class PaymentOrderRequestDTO {

    @NotNull(message = "Plan type is required (BASIC, PRO, ENTERPRISE)")
    private PlanType planType;

    @NotNull(message = "Billing cycle is required (MONTHLY, YEARLY)")
    private BillingCycle billingCycle;
}
