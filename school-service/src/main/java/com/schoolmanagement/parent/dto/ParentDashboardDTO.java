package com.schoolmanagement.parent.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentDashboardDTO {

    // ── Ward (Student) Profile ──
    private Long studentDbId;
    private String studentName;
    private String studentUid;
    private String rollNumber;
    private String dateOfBirth;
    private String standard;
    private String division;
    private String profilePhoto;
    private String academicYear;

    // ── Today's Attendance ──
    private String todayAttendanceStatus; // PRESENT, ABSENT, NOT_MARKED

    // ── Overall Attendance ──
    private long totalAttendanceDays;
    private long presentDays;
    private long absentDays;
    private double attendancePercentage;

    // ── Recent Marks ──
    private List<RecentMarkDTO> recentMarks;

    // ── Notices ──
    private List<NoticeDTO> notices;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentMarkDTO {
        private String examName;
        private String subjectName;
        private Double marksObtained;
        private Double maxMarks;
        private String grade;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NoticeDTO {
        private String title;
        private String message;
        private String type;
        private String sentAt;
    }
}
