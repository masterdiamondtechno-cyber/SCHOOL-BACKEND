package com.schoolmanagement.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "SaasSubscriptionPlan")
@Table(name = "saas_subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Plan name: BASIC, PRO, ENTERPRISE
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private PlanType planType;

    @Column(nullable = false)
    private String displayName; // "Basic Plan", "Pro Plan", "Enterprise Plan"

    @Column(nullable = false)
    private String description;

    // Monthly price in rupees
    @Column(nullable = false)
    private BigDecimal monthlyPrice;

    // Yearly price in rupees (usually discounted)
    @Column(nullable = false)
    private BigDecimal yearlyPrice;

    // Max students allowed under this plan
    @Column(nullable = false)
    private Integer maxStudents;

    // Max teachers allowed
    @Column(nullable = false)
    private Integer maxTeachers;

    // Max staff allowed
    @Column(nullable = false)
    private Integer maxStaff;

    // Features flags — kya kya milega is plan mein
    @Column(nullable = false)
    private Boolean smsNotifications;       // SMS bhej sakte ho?

    @Column(nullable = false)
    private Boolean videoLectures;          // S3 video lectures upload kar sakte ho?

    @Column(nullable = false)
    private Boolean studyMaterials;         // Study material upload?

    @Column(nullable = false)
    private Boolean advancedReports;        // Detailed analytics?

    @Column(nullable = false)
    private Boolean customBranding;         // School ka logo/branding?

    @Column(nullable = false)
    private Boolean prioritySupport;        // Fast customer support?

    @Column(nullable = false)
    private Boolean apiAccess;              // Third-party API access?

    @Column(nullable = false)
    private Boolean isActive;              // Ye plan abhi available hai?

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Plan types
    public enum PlanType {
        BASIC,
        PRO,
        ENTERPRISE
    }
}
