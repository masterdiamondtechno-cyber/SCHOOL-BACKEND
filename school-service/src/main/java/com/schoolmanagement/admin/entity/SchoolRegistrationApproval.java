package com.schoolmanagement.admin.entity;

import com.schoolmanagement.school.entity.School;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "school_registration_approval")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchoolRegistrationApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long approvalId;

    @ManyToOne
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    private String schoolName;
    private String udiseNo;
    private String email;
    private String phone;

    @Column(nullable = true)
    private String approvalRejectionStatus = "PENDING"; // APPROVED / REJECTED

    @Column(nullable = true, length = 500)
    private String rejectedReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;
}
