package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassTimetableResponseDTO {

    private Long schoolId;
    private Long academicYearId;
    private String date;
    private String dayOfWeek;
}