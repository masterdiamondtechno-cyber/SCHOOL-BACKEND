package com.schoolmanagement.owner.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerFinanceBranchDTO {

    private Long schoolId;
    private String schoolName;
    private double collected;
    private double billed;
    private double pending;
    private long studentCount;

}
