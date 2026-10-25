package com.schoolmanagement.parent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectProgressDTO {

    private String subjectName;
    private Double scoreObtained;
    private Double totalMarks;
    private String grade;
    private Double classAverage;
    private String status;
}
