package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeeStructureDTO {

    private Long feeStructureId;
    private Long schoolId;
    private Long academicYearId;
    private Long standardId;
    private String feeName;
    private String feeType;
    private Double amount;
    private LocalDate dueDate;
    private String description;

}
