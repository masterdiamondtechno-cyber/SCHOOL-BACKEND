package com.schoolmanagement.owner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerDashboardDTO {

    private long totalBranches;
    private long totalStudents;
    private double totalRevenue;
    private double growthRate;
    private int filterYear;
    private Integer filterMonth;

    private List<Map<String, Object>> performanceMatrix;
    private List<Map<String, Object>> recentAlerts;

}
