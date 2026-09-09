package com.schoolmanagement.school_admin_Service;

import java.util.List;

import com.schoolmanagement.school_admin_DTO.AddSubjectDTO;

public interface AddSubjectService {

    AddSubjectDTO addSubject(AddSubjectDTO dto);

    List<AddSubjectDTO> getAllBySchoolStandardDivision(
            Long schoolId,
            Long standardId,
            Long divisionId,
            Long academicYearId
    );

    AddSubjectDTO getBySubjectId(Long subjectId, Long academicYearId);

    List<AddSubjectDTO> getAllBySchoolId(Long schoolId, Long academicYearId);

    AddSubjectDTO updateSubject(AddSubjectDTO dto);

    void deleteSubject(Long subjectId);

    List<AddSubjectDTO> getByTeacherId(Long teacherId, Long academicYearId);
}
