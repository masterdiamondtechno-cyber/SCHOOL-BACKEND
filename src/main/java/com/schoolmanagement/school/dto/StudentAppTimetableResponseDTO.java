package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentAppTimetableResponseDTO {

    private Integer code;
    private String status;
    private String message;
    private Object data;


    // ==================== Day View ====================
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DayViewData {
        private String type;
        private String date;
        private Boolean isToday;
        private String serverTime;
        private String dayStatus;
        private List<ScheduleEntry> schedule;
    }

    // ==================== Week View ====================
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeekViewData {
        private String type;
        private String startDate;
        private String endDate;
        private List<DaySchedule> days;
    }

    // ==================== Day Schedule (for week view) ====================
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DaySchedule {
        private String date;
        private String dayName;
        private String dayStatus;
        private List<ScheduleEntry> schedule;
    }

    // ==================== Schedule Entry ====================
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScheduleEntry {
        private Long periodId;
        private Integer periodNumber;
        private String startTime;
        private String endTime;
        private String subject;
        private String teacher;
        private String room;
        private Boolean isSubstitute;
        private String originalTeacher;
        private Boolean isCancelled;
        private String status;
    }
}
