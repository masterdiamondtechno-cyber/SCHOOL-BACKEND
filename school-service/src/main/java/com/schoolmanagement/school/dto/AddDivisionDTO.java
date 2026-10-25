package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddDivisionDTO {

    private Long divisionId;
    private Long schoolId;
    private Long academicYearId;
    private String schoolName;
    private Long standardId;
    private String standard;
    private String divisionName;
    private String description;
    private Long classTeacherId;
    private String classTeacherName;
}
