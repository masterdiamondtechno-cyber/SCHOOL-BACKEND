package com.schoolmanagement.subscription.dto;

import com.schoolmanagement.subscription.entity.SchoolSubscription.BillingCycle;
import com.schoolmanagement.subscription.entity.SubscriptionPlan.PlanType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOrderResponseDTO {

    private String orderId;              // Razorpay order ID (or order_mock_xxx)
    private Long schoolId;
    private PlanType planType;
    private BillingCycle billingCycle;
    private BigDecimal amount;            // Rupee amount (e.g. 2999.00)
    private Long amountInPaise;           // In paise for Razorpay checkout (e.g. 299900)
    private String currency;              // "INR"
    private String razorpayKeyId;         // Public key for frontend Razorpay modal
    private boolean isMockMode;           // true if simulated test mode
    private String message;
}
