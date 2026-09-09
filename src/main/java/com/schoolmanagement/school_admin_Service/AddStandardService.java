package com.schoolmanagement.school_admin_Service;

import java.util.List;

import com.schoolmanagement.school_admin_DTO.AddStandardDTO;

public interface AddStandardService {

    AddStandardDTO addStandard(AddStandardDTO dto);

    List<AddStandardDTO> getAllBySchoolId(Long schoolId, Long academicYearId);

    AddStandardDTO getByStandardId(Long standardId, Long academicYearId);

    AddStandardDTO updateStandard(AddStandardDTO dto);

    void deleteStandard(Long standardId);
}
