package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherAppTimetableRequestDTO {

    private Long teacherId;
    private Long schoolId;
    private Long academicYearId;
    private String type;
    private String date;

}
