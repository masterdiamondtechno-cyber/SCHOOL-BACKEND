package com.schoolmanagement.parent_DTO;

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
