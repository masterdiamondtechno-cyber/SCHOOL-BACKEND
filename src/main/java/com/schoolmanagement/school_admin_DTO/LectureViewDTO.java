package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning student lecture view information.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LectureViewDTO {

    private Long studentId;
    private String studentName;
    private String rollNo;
    private String standardName;
    private String divisionName;
    private String viewedAt;

}
