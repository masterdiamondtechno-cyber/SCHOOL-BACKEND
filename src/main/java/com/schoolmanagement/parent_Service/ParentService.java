package com.schoolmanagement.parent_Service;

import com.schoolmanagement.parent_DTO.AcademicProgressDTO;

public interface ParentService {

    AcademicProgressDTO getReportCard(Long schoolId, Long academicYearId, Long studentId);
}
