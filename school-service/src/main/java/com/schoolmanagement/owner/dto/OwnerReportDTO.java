package com.schoolmanagement.owner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerReportDTO {

    private String reportType;   // revenue | attendance | enrollment | fees
    private String startDate;
    private String endDate;
    private String groupBy;      // day | month | year
    private Long schoolId;
    private Long classId;
    private List<Map<String, Object>> data;

}
