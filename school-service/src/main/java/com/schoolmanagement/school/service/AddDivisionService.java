package com.schoolmanagement.school.service;

import java.util.List;

import com.schoolmanagement.school.dto.AddDivisionDTO;

public interface AddDivisionService {

    AddDivisionDTO addDivision(AddDivisionDTO dto);

    List<AddDivisionDTO> getAllBySchoolId(Long schoolId, Long academicYearId);

    List<AddDivisionDTO> getAllByStandardId(Long standardId, Long academicYearId);

    AddDivisionDTO getByDivisionId(Long divisionId, Long academicYearId);

    AddDivisionDTO updateDivision(AddDivisionDTO dto);

    void deleteDivision(Long divisionId);
}
