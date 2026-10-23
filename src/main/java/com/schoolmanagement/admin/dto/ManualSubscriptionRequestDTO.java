package com.schoolmanagement.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManualSubscriptionRequestDTO {
    private Long schoolId;
    private Long planId;
    private String startDate; // ISO-8601 string, e.g., "2026-06-15T00:00:00"
    private String endDate;   // ISO-8601 string, e.g., "2026-12-15T00:00:00"
    private String duration;
    private String status;    // ACTIVE or INACTIVE
    private String reason;
    private Long assignedBy;
}
