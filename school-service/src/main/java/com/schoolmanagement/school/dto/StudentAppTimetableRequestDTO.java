package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentAppTimetableRequestDTO {

    private Long studentId;
    private Long schoolId;
    private Long academicYearId;
    private String type;  // "day" or "week"
    private String date;  // yyyy-MM-dd

}
