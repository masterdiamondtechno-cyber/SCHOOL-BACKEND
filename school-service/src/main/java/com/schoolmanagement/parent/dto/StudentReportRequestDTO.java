package com.schoolmanagement.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentReportRequestDTO {

    private Long schoolId;
    private Long academicYearId;
    private Long studentId;
}
