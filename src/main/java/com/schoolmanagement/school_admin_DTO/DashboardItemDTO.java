package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardItemDTO {

    private Long dashboardItemId;
    private String dashboardItemName;
    private String role;

    // For /dashboard/data request
    private Long schoolId;
    private Long academicYearId;
    private Long teacherId;
    private Long studentId;

    // ===== Teacher Dashboard fields =====
    private String teacherUsername;
    private LocalDate scheduleDate;   // defaults to today if null

}
