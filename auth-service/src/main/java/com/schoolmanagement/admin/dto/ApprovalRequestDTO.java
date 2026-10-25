package com.schoolmanagement.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalRequestDTO {

    private Long schoolId;
    private String approvalRejectionStatus; // APPROVED / REJECTED
    private String rejectedReason;
    private Long ownerId;
}
