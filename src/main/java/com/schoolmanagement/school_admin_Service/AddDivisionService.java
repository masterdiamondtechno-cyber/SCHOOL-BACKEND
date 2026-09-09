package com.schoolmanagement.school_admin_Service;

import java.util.List;

import com.schoolmanagement.school_admin_DTO.AddDivisionDTO;

public interface AddDivisionService {

    AddDivisionDTO addDivision(AddDivisionDTO dto);

    List<AddDivisionDTO> getAllBySchoolId(Long schoolId, Long academicYearId);

    List<AddDivisionDTO> getAllByStandardId(Long standardId, Long academicYearId);

    AddDivisionDTO getByDivisionId(Long divisionId, Long academicYearId);

    AddDivisionDTO updateDivision(AddDivisionDTO dto);

    void deleteDivision(Long divisionId);
}
