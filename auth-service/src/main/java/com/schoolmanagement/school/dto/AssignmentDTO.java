package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDTO {

    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private String attachmentUrl;
    private Long subjectId;
    private String subjectName;
    private Long standardId;
    private String standardName;
    private Long divisionId;
    private String divisionName;
    private Long teacherId;
    private String teacherName;
    private Long schoolId;
    private Long academicYearId;
    private Long studyGroupId;
    private String studyGroupName;
    private String status;
    private LocalDateTime createdAt;
}
