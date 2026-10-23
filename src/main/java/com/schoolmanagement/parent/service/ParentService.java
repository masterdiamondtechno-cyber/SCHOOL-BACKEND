package com.schoolmanagement.parent.service;

import com.schoolmanagement.parent.dto.AcademicProgressDTO;

public interface ParentService {

    AcademicProgressDTO getReportCard(Long schoolId, Long academicYearId, Long studentId);
}
