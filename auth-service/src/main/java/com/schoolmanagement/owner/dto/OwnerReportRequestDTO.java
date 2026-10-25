package com.schoolmanagement.owner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerReportRequestDTO {
    private Long ownerId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long schoolId;
    private Long classId;
    private String groupBy = "month";

}
