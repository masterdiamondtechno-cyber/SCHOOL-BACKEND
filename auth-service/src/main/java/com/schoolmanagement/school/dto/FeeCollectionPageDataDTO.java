package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeeCollectionPageDataDTO {

    private FeeCollectionSummaryDTO summary;
    private List<FeePaymentDTO> records;

}
