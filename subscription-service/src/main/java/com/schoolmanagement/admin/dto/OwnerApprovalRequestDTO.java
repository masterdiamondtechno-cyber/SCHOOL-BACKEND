package com.schoolmanagement.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerApprovalRequestDTO {

    private Long ownerId;
    private String approvalRejectionStatus; // APPROVED / REJECTED
    private String rejectedReason;
}
