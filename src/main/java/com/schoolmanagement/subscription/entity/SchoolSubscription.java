package com.schoolmanagement.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "school_subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Kaun school hai ye subscription
    @Column(nullable = false)
    private Long schoolId;

    // Kaunsa plan liya hai
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    // Monthly ya Yearly
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingCycle billingCycle;

    // Subscription ki status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    // Kab se kab tak valid hai
    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    // Trial period
    private LocalDate trialEndDate;

    @Column(nullable = false)
    private Boolean isTrialPeriod;

    // Razorpay subscription ID (auto-renewal ke liye)
    private String razorpaySubscriptionId;

    // Razorpay customer ID
    private String razorpayCustomerId;

    // Last payment date
    private LocalDate lastPaymentDate;

    // Next renewal date
    private LocalDate nextRenewalDate;

    // Cancellation reason (agar cancel kiya)
    private String cancellationReason;
    private LocalDateTime cancelledAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isTrialPeriod == null) isTrialPeriod = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ─── Enums ─────────────────────────────

    public enum BillingCycle {
        MONTHLY,
        YEARLY
    }

    public enum SubscriptionStatus {
        TRIAL,          // 14 din free trial chal raha hai
        ACTIVE,         // Payment ho gayi, use kar sakte ho
        EXPIRED,        // Renewal nahi kiya — access band
        CANCELLED,      // School ne khud cancel kiya
        SUSPENDED,      // Payment fail — temporarily band
        PENDING_PAYMENT // Payment pending hai
    }
}
