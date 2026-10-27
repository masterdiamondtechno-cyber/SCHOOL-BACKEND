package com.schoolmanagement.parent.service;

import java.util.List;

import com.schoolmanagement.parent.dto.AcademicProgressDTO;
import com.schoolmanagement.parent.dto.ParentDashboardDTO;
import com.schoolmanagement.parent.dto.ParentTeacherMessageDTO;
import com.schoolmanagement.parent.dto.SendMessageRequestDTO;

public interface ParentService {

    AcademicProgressDTO getReportCard(Long schoolId, Long academicYearId, Long studentId);

    ParentDashboardDTO getDashboardOverview(Long schoolId, Long studentId, Long academicYearId);

    ParentTeacherMessageDTO sendMessage(SendMessageRequestDTO request);

    List<ParentTeacherMessageDTO> getStudentConversation(Long schoolId, Long studentId);

    List<ParentTeacherMessageDTO> getTeacherMessages(Long schoolId, Long teacherId);
}
