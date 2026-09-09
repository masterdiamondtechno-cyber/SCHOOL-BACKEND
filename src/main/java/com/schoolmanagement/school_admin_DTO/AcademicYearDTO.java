package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AcademicYearDTO {

    private Long academicYearId;
    private Long schoolId;
    private String yearName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent;
    private String status;
}
