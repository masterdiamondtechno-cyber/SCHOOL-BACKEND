package com.schoolmanagement.school_admin_Service;

import com.schoolmanagement.school_admin_DTO.StudentLeaveDTO;

import java.util.List;
import java.util.Map;

public interface StudentLeaveService {

    // ── Student actions ────────────────────────────────────────────────────────
    void applyLeave(StudentLeaveDTO dto);           // JWT-identified student applies
    void cancelLeave(Long leaveId, Long schoolId);  // JWT-identified student cancels PENDING leave

    // ── School actions ─────────────────────────────────────────────────────────
    void approveLeave(StudentLeaveDTO dto);          // schoolId, leaveId, reviewedBy, reviewRemark
    void rejectLeave(StudentLeaveDTO dto);           // schoolId, leaveId, reviewedBy, reviewRemark

    // ── Read: Student view ─────────────────────────────────────────────────────
    List<Map<String, Object>> getMyLeaves(Long studentId, Long schoolId, String statusFilter);
    Map<String, Object> getMyStats(Long studentId, Long schoolId, Long academicYearId);

    // ── Read: School view ─────────────────────────────────────────────────────
    List<Map<String, Object>> getAll(StudentLeaveDTO dto);       // with filters: status, leaveType, date range
    List<Map<String, Object>> getPending(Long schoolId, Long academicYearId);
    Map<String, Object> getById(Long leaveId, Long schoolId);
    List<Map<String, Object>> getByStudent(Long studentId, Long schoolId, Long academicYearId);
    Map<String, Object> getSchoolStats(Long schoolId, Long academicYearId);

    // ── Read: Teacher (Class Teacher) view ─────────────────────────────────────
    List<Map<String, Object>> getClassStudentLeaves(Long schoolId, String statusFilter);
}
