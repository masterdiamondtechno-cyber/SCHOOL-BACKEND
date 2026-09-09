package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherTimetableResponseDTO {

    private TeacherInfo teacher;
    private List<ScheduleEntry> todaySchedule;
    private ScheduleEntry nextLecture;
    private List<AnnouncementEntry> announcements;

    // ==================== INNER CLASSES ====================

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TeacherInfo {
        private Long id;
        private String name;
        private String profileImage;
        private List<SubjectInfo> subjects;
        private List<ClassDivision> classDivisions;

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
    public static class ClassDivision {
        private String className;
        private String division;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScheduleEntry {
        private Long periodId;
        private String startTime;
        private String endTime;
        private String className;
        private String division;
        private String subject;
        private String room;
        private String status; // upcoming / ongoing / completed
        private Boolean isSubstitute;
        private String originalTeacher;
        private Boolean isCancelled;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnnouncementEntry {
        private Long id;
        private String title;
        private String date;
    }

}