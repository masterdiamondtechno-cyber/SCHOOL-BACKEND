package com.schoolmanagement.owner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerSchoolStatsDTO {

    private long totalSchools;
    private long activeSchools;
    private long inactiveSchools;
    private long totalStudents;
    private long totalTeachers;
    private double totalRevenue;
    private double avgStudentsPerSchool;
    private double avgRevenuePerSchool;

}
