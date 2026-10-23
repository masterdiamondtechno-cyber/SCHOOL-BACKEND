package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddStandardDTO {

    private Long standardId;
    private Long schoolId;
    private Long academicYearId;
    private String schoolName;
    private String standard;
    private String description;
    private Long classTeacherId;
    private String classTeacherName;
    private Integer sectionCount;
    private List<SectionSummaryDTO> sections;
}
