package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamDTO {

    private Long examId;
    private Long schoolId;
    private Long academicYearId;
    private Long standardId;
    private Long divisionId;
    private Long subjectId;
    private String subjectName;
    private String examName;
    private String examMode; // THEORY, PRACTICAL
    private LocalDate examDate;
    private String duration; // e.g. "3 Hours"
    private Double totalMarks;
    private String status; // UPCOMING, COMPLETED, CANCELLED

    // Student-facing fields
    private Long studentId;
    private Double marksObtained;
    private String score; // e.g. "92/100"

}
