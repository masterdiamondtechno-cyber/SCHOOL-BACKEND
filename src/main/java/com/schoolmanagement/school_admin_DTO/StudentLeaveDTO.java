package com.schoolmanagement.school_admin_DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentLeaveDTO {

    // ── Request fields ─────────────────────────────────────────────────────────
    private Long id;
    private Long schoolId;
    private Long academicYearId;
    private Long studentId;              // student ID passed from client or resolved
    private String leaveType;            // SICK | CASUAL | EMERGENCY | OTHER
    private LocalDate fromDate;
    private LocalDate toDate;
    private String reason;
    private String status;               // PENDING | APPROVED | REJECTED | CANCELLED
    private String reviewedBy;
    private String reviewRemark;

    // ── Filter fields ──────────────────────────────────────────────────────────
    private String statusFilter;         // ALL | PENDING | APPROVED | REJECTED | CANCELLED
    private String leaveTypeFilter;
    private LocalDate filterFromDate;
    private LocalDate filterToDate;

    // ── Response-only fields ───────────────────────────────────────────────────
    private String leaveRefId;
    private Integer totalDays;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private String studentName;

}
