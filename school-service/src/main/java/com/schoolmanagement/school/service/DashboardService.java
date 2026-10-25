package com.schoolmanagement.school.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.schoolmanagement.school.dto.DashboardItemDTO;

public interface DashboardService {

    void saveItem(DashboardItemDTO dto);

    List<Map<String, Object>> getDashboardData(Long schoolId, Long academicYearId, String role, Long teacherId, Long studentId);

    Object resolveAdminData(String itemName);

    Map<String, Object> getStudentInfo(Long schoolId, Long academicYearId, Long studentId);

    // ===== Teacher Dashboard =====
    Map<String, Object> getTeacherStats(Long schoolId, Long academicYearId, Long teacherId);

    List<Map<String, Object>> getTeacherSchedule(Long schoolId, Long academicYearId, Long teacherId, LocalDate date);

    List<Map<String, Object>> getTeacherGradingTasks(Long schoolId, Long academicYearId, Long teacherId);
}
