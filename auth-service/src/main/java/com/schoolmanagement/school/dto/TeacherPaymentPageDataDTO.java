package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherPaymentPageDataDTO {

    // Summary card values
    private Double totalPayroll;
    private Double totalDisbursed;
    private Double totalPending;
    private Long pendingCount;
    private String payPeriod;

    // Table data
    private List<Map<String, Object>> payments;
}
