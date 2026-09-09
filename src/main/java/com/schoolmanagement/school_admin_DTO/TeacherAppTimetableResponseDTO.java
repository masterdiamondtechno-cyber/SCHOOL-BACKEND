package com.schoolmanagement.school_admin_DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherAppTimetableResponseDTO {
    private Integer code;
    private String status;
    private String message;
    private Object data;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DayViewData {
        private String type;
        private String view;
        private String date;
        private Boolean isToday;
        private String serverTime;
        private String dayStatus;
        private PeriodConfig periodConfig;
        private List<ScheduleEntry> schedule;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WeekViewData {
        private String type;
        private String view;
        private String startDate;
        private String endDate;
        private PeriodConfig periodConfig;
        private List<DaySchedule> days;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DaySchedule {
        private String date;
        private String dayName;
        private String dayStatus;
        private List<ScheduleEntry> schedule;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PeriodConfig {
        private Integer totalPeriods;
        private Integer breakAfterPeriod;

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScheduleEntry {
        private Long periodId;
        private String startTime;
        private String endTime;

        @JsonProperty("class")
        private String className;

        private String division;
        private String subject;
        private String room;

        private Boolean isSubstitute;
        private String originalTeacher;
        private Boolean isCancelled;
        private String status;
    }
}
