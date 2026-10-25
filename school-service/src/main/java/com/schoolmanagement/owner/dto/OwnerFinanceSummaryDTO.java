package com.schoolmanagement.owner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerFinanceSummaryDTO {

    private double totalBilled;
    private double totalCollected;
    private double pendingAmount;
    private long transactionCount;
    private String startDate;
    private String endDate;

}
