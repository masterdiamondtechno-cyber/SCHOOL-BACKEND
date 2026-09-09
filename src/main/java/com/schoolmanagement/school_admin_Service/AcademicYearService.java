package com.schoolmanagement.school_admin_Service;

import java.util.List;
import com.schoolmanagement.school_admin_DTO.AcademicYearDTO;

public interface AcademicYearService {
    AcademicYearDTO create(AcademicYearDTO dto);
    List<AcademicYearDTO> getAllBySchoolId(Long schoolId);
    AcademicYearDTO getById(Long academicYearId, Long schoolId);
    AcademicYearDTO getCurrent(Long schoolId);
    AcademicYearDTO update(AcademicYearDTO dto);
    void delete(Long academicYearId, Long schoolId);
    
    // System Requirements
    void activate(Long academicYearId, Long schoolId);
    void archive(Long academicYearId, Long schoolId);
}
