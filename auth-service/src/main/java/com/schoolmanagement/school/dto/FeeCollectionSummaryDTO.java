package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeeCollectionSummaryDTO {

    private Double totalCollected;
    private Double totalPending;
    private Long paidCount;
    private Long partialCount;
    private Long unpaidCount;

}
