package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudyMaterialDTO {

    // ── Response / Read fields ────────────────────────────────────────────────
    private Long id;
    private String materialId;        // SM001, SM002 ...
    private String fileType;          // PDF, DOC, ZIP, PPT
    private String fileSize;          // 3.5 MB
    private String fileUrl;           // Presigned S3 URL for download
    private Long downloadCount;
    private String uploadDate;        // formatted: 20 Mar 2026
    private String classDisplay;      // composed:  "10-A"
    private String uploadedByRole;    // SCHOOL | TEACHER
    private String teacherName;
    private String subjectName;
    private String standardName;
    private String divisionName;

    // ── Request / Write fields ────────────────────────────────────────────────
    private String title;
    private String materialType;      // Notes / Worksheet / Practice Sheet / Reference / Assignment / Revision
    private String status;            // Published / Draft

    private Long schoolId;
    private Long academicYearId;
    private Long subjectId;
    private Long standardId;
    private Long divisionId;
    private Long teacherId;

}
