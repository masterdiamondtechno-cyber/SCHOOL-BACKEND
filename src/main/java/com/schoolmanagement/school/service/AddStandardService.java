package com.schoolmanagement.school.service;

import java.util.List;

import com.schoolmanagement.school.dto.AddStandardDTO;

public interface AddStandardService {

    AddStandardDTO addStandard(AddStandardDTO dto);

    List<AddStandardDTO> getAllBySchoolId(Long schoolId, Long academicYearId);

    AddStandardDTO getByStandardId(Long standardId, Long academicYearId);

    AddStandardDTO updateStandard(AddStandardDTO dto);

    void deleteStandard(Long standardId);
}
