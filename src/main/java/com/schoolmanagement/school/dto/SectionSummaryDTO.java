package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectionSummaryDTO {
    private Long divisionId;
    private String divisionName;
    private Long classTeacherId;
    private String classTeacherName;

}
