package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddSubjectDTO {

    private Long subjectId;
    private Long schoolId;
    private Long academicYearId;
    private String academicYearName;
    private String schoolName;
    private Long standardId;
    private String standard;
    private Long divisionId;
    private String divisionName;
    private String subjectName;
    private String description;
    private Long teacherId;
    private String teacherName;
}
