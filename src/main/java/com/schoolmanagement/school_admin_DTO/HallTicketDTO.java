package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HallTicketDTO {

    private String studentName;
    private String studentId;
    private String standardName;
    private String divisionName;
    private String schoolName;
    private String examName;
    private String subjectName;
    private String examMode;
    private LocalDate examDate;
    private String duration;
    private Double totalMarks;
    private String status;

}
