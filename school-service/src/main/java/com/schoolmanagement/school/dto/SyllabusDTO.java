package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SyllabusDTO {

    private Long id;
    private String syllabusId;
    private String title;

    // FK IDs (for request)
    private Long schoolId;
    private Long academicYearId;
    private Long subjectId;
    private Long standardId;
    private Long teacherId;

    // Display names (for response)
    private String subjectName;
    private String standardName;
    private String teacherName;

    // Progress
    private Integer totalChapters;
    private Integer completedChapters;
    private Integer progress;           // calculated: (completed/total)*100

    // Status filter
    private String status;

    // File info
    private String fileType;
    private String fileSize;
    private String fileUrl;             // presigned S3 URL

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
