package com.schoolmanagement.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherLeaveDTO {

    // ── Request fields ─────────────────────────────────────────────────────────
    private Long id;
    private Long schoolId;
    private Long academicYearId;
    private Long teacherId;              // used by school-side endpoints; teacher endpoints use JWT
    private Long substituteTeacherId;    // improvement: substitute assignment
    private String leaveType;            // SICK | CASUAL | EMERGENCY | MATERNITY | UNPAID | OTHER
    private LocalDate fromDate;
    private LocalDate toDate;
    private String reason;
    private String status;               // PENDING | APPROVED | REJECTED | CANCELLED
    private String reviewedBy;
    private String reviewRemark;

    // ── Filter fields (for getAll / getSchoolStats) ────────────────────────────
    private String statusFilter;         // filter by status
    private String leaveTypeFilter;      // filter by leave type
    private LocalDate filterFromDate;    // date range filter start
    private LocalDate filterToDate;      // date range filter end

    // ── Response-only fields ───────────────────────────────────────────────────
    private String leaveRefId;
    private Integer totalDays;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private String teacherName;
    private String substituteTeacherName;

}