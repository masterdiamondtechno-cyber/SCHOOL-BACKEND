package com.schoolmanagement.parent.service;

import com.schoolmanagement.parent.dto.AcademicProgressDTO;
import com.schoolmanagement.parent.dto.ParentDashboardDTO;

public interface ParentService {

    AcademicProgressDTO getReportCard(Long schoolId, Long academicYearId, Long studentId);

    ParentDashboardDTO getDashboardOverview(Long schoolId, Long studentId, Long academicYearId);
}
