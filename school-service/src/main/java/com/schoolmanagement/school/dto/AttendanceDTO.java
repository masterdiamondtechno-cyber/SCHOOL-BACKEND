package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceDTO {

    private Long attendanceId;
    private Long schoolId;
    private Long academicYearId;
    private Long standardId;
    private Long divisionId;
    private Long subjectId;
    private Long studentId;
    private Long markedBy;
    private LocalDate attendanceDate;
    private String status;
    private String remark;
    private LocalDateTime markedAt;

    // For bulk marking
    private List<AttendanceEntryDTO> entries;

    // Inner DTO for bulk entries
    public static class AttendanceEntryDTO {
        private Long studentId;
        private String status;
        private String remark;

        public Long getStudentId() {
            return studentId;
        }

        public void setStudentId(Long studentId) {
            this.studentId = studentId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }

    // For report filters
    private Integer month;
    private Integer year;
    private LocalDate fromDate;
    private LocalDate toDate;

}
