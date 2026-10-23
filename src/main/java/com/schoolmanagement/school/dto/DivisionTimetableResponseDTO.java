package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DivisionTimetableResponseDTO {

    private Long schoolId;
    private Long academicYearId;
    private Long standardId;
    private Long divisionId;
    private List<PeriodEntry> timetable;

    // ==================== INNER CLASSES ====================

    public static class PeriodEntry {
        private Long periodId;
        private Integer periodNumber;
        private String dayOfWeek;
        private String startTime;
        private String endTime;
        private SubjectInfo subject;
        private TeacherInfo teacher;
        private String roomNumber;
        private Boolean isSubstitute;
        private String originalTeacher;
        private Boolean isCancelled;
        private String status;

        public Long getPeriodId() {
            return periodId;
        }

        public void setPeriodId(Long periodId) {
            this.periodId = periodId;
        }

        public Integer getPeriodNumber() {
            return periodNumber;
        }

        public void setPeriodNumber(Integer periodNumber) {
            this.periodNumber = periodNumber;
        }

        public String getDayOfWeek() {
            return dayOfWeek;
        }

        public void setDayOfWeek(String dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public SubjectInfo getSubject() {
            return subject;
        }

        public void setSubject(SubjectInfo subject) {
            this.subject = subject;
        }

        public TeacherInfo getTeacher() {
            return teacher;
        }

        public void setTeacher(TeacherInfo teacher) {
            this.teacher = teacher;
        }

        public String getRoomNumber() {
            return roomNumber;
        }

        public void setRoomNumber(String roomNumber) {
            this.roomNumber = roomNumber;
        }

        public Boolean getIsSubstitute() {
            return isSubstitute;
        }

        public void setIsSubstitute(Boolean isSubstitute) {
            this.isSubstitute = isSubstitute;
        }

        public String getOriginalTeacher() {
            return originalTeacher;
        }

        public void setOriginalTeacher(String originalTeacher) {
            this.originalTeacher = originalTeacher;
        }

        public Boolean getIsCancelled() {
            return isCancelled;
        }

        public void setIsCancelled(Boolean isCancelled) {
            this.isCancelled = isCancelled;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class SubjectInfo {
        private Long id;
        private String name;

        public SubjectInfo(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class TeacherInfo {
        private Long id;
        private String name;

        public TeacherInfo(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
