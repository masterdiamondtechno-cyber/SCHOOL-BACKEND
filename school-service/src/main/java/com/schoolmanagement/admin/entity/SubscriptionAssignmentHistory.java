package com.schoolmanagement.admin.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subscription_assignment_history")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionAssignmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "plan_id")
    private Long planId;

    private String planName;

    private String subscriptionSource;

    @Column(name = "assigned_by")
    private Long assignedBy;

    private String assignedByName;

    private LocalDateTime assignmentDate;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private String duration;

    @Column(length = 1024)
    private String reason;

    @Column(length = 2048)
    private String previousSubscriptionState;

    @Column(length = 2048)
    private String newSubscriptionState;
}
