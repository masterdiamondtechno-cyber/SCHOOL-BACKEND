package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchoolTimetableResponseDTO {

    private Long schoolId;
    private Long academicYearId;
    private String date;
    private String dayOfWeek;
    private List<SchoolPeriodEntry> timetable;

    // ==================== INNER CLASSES ====================
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SchoolPeriodEntry {
        private Long periodId;
        private Integer periodNumber;
        private String startTime;
        private String endTime;
        private ClassInfo classInfo;
        private SubjectInfo subject;
        private TeacherInfo teacher;
        private String roomNumber;
        private Boolean isSubstitute;
        private String originalTeacher;
        private Boolean isCancelled;
        private String status;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ClassInfo {
        private Long standardId;
        private String className;
        private Long batchId;
        private String division;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubjectInfo {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TeacherInfo {
        private Long id;
        private String name;
    }

}