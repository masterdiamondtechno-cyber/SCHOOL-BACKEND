package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordedLectureDTO {

    // ── Response / Read fields ────────────────────────────────────────────────
    private Long id;
    private String lectureId;          // RL001, RL002 ...
    private String fileType;           // MP4, AVI, MOV
    private String fileSize;           // 22 MB
    private String fileUrl;            // Presigned S3 URL for streaming
    private Long viewCount;
    private String recordedDate;       // formatted: 20 Mar 2026
    private String classDisplay;       // composed: "10-A"
    private String teacherName;
    private String subjectName;
    private String standardName;
    private String divisionName;

    // ── Request / Write fields ────────────────────────────────────────────────
    private String title;
    private String status;             // Published / Processing / Draft
    private Integer duration;          // Duration in minutes

    private Long schoolId;
    private Long academicYearId;
    private Long subjectId;
    private Long standardId;
    private Long divisionId;
    private Long teacherId;

}
