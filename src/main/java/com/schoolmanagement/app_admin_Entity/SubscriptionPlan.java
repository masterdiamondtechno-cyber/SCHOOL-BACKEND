package com.schoolmanagement.app_admin_Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_plans")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;

    @Column(nullable = false, unique = true)
    private String planName;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private String billingCycle; // YEARLY, MONTHLY

    private Integer maxStudents; // -1 for unlimited
    private Integer maxTeachers; // -1 for unlimited
    private Integer storageLimitGb;

    @Column(columnDefinition = "TEXT")
    private String features; // Comma-separated or JSON string

    private String supportLevel;
    private Boolean isMostPopular = false;
    private Integer discountPercentage;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(length = 1024)
    private String paymentLink;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
