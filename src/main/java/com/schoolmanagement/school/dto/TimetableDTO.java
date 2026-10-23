package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimetableDTO {

    private Long timetableId;
    private Long schoolId;
    private Long academicYearId;
    private Long standardId;
    private Long divisionId;
    private Long subjectId;
    private Long teacherId;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer periodNumber;
    private String roomNumber;
    private String date;
    private Boolean isSubstitute;
    private String originalTeacher;
    private Boolean isCancelled;
    private String status;
    private String type;

}