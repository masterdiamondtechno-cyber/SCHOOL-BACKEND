package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentSubmissionDTO {

    private Long id;
    private Long assignmentId;
    private String assignmentTitle;
    private Long studentId;
    private String studentName;
    private Long schoolId;
    private Long academicYearId;
    private String fileUrl;
    private LocalDateTime submissionDate;
    private String status;
    private String remarks;
    private Double marks;
    private LocalDateTime createdAt;

}
