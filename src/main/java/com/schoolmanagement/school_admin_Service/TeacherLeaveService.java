package com.schoolmanagement.school_admin_Service;

import com.schoolmanagement.school_admin_DTO.TeacherLeaveDTO;

import java.util.List;
import java.util.Map;

public interface TeacherLeaveService {

    // ── Teacher actions ────────────────────────────────────────────────────────
    void applyLeave(TeacherLeaveDTO dto);           // JWT-identified teacher applies
    void cancelLeave(Long leaveId, Long schoolId);  // JWT-identified teacher cancels PENDING leave

    // ── School actions ─────────────────────────────────────────────────────────
    void approveLeave(TeacherLeaveDTO dto);          // schoolId, leaveId, reviewedBy, reviewRemark, optional substituteTeacherId
    void rejectLeave(TeacherLeaveDTO dto);           // schoolId, leaveId, reviewedBy, reviewRemark

    // ── Read: Teacher view ─────────────────────────────────────────────────────
    List<Map<String, Object>> getMyLeaves(Long teacherId, Long schoolId, String statusFilter);
    Map<String, Object> getMyStats(Long teacherId, Long schoolId, Long academicYearId);

    // ── Read: School view ─────────────────────────────────────────────────────
    List<Map<String, Object>> getAll(TeacherLeaveDTO dto);       // with filters: status, leaveType, date range
    List<Map<String, Object>> getPending(Long schoolId, Long academicYearId);
    Map<String, Object> getById(Long leaveId, Long schoolId);
    List<Map<String, Object>> getByTeacher(Long teacherId, Long schoolId, Long academicYearId);
    Map<String, Object> getSchoolStats(Long schoolId, Long academicYearId);
}