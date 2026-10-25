package com.schoolmanagement.subscription.dto;

import com.schoolmanagement.subscription.entity.SchoolSubscription.BillingCycle;
import com.schoolmanagement.subscription.entity.SubscriptionPlan.PlanType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVerifyRequestDTO {

    @NotBlank(message = "Razorpay order ID is required")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay payment ID is required")
    private String razorpayPaymentId;

    // Razorpay signature (optional in mock mode, required in real mode)
    private String razorpaySignature;

    @NotNull(message = "Plan type is required")
    private PlanType planType;

    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;

    private String paymentMethod; // card, upi, netbanking, mock
}
