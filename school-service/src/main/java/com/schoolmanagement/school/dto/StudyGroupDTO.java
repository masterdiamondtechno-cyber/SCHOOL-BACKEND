package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudyGroupDTO {

    private Long id;
    private String groupName;
    private String description;
    private Long createdByTeacherId;
    private String createdByTeacherName;
    private Long standardId;
    private String standardName;
    private Long divisionId;
    private String divisionName;
    private Long schoolId;
    private Long academicYearId;
    private Boolean isActive;
    private Long memberCount;
    private LocalDateTime createdAt;

}
