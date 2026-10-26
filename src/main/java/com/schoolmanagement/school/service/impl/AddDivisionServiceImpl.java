package com.schoolmanagement.school.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.schoolmanagement.school.dto.AddDivisionDTO;
import com.schoolmanagement.school.entity.AcademicYear;
import com.schoolmanagement.school.entity.AddDivision;
import com.schoolmanagement.school.entity.AddStandard;
import com.schoolmanagement.school.entity.AddTeacher;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddDivisionRepository;
import com.schoolmanagement.school.repository.AddStandardRepository;
import com.schoolmanagement.school.repository.AddSubjectRepository;
import com.schoolmanagement.school.repository.AddTeacherRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.service.AddDivisionService;

@Service
public class AddDivisionServiceImpl implements AddDivisionService {

    @Autowired
    private AddDivisionRepository repository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private AddStandardRepository standardRepository;

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private AddSubjectRepository subjectRepository;

    @Autowired
    private AddTeacherRepository teacherRepository;

    // ==================== MAPPER ====================

    private AddDivisionDTO mapToDTO(AddDivision entity) {
        AddDivisionDTO dto = new AddDivisionDTO();
        dto.setDivisionId(entity.getDivisionId());
        dto.setSchoolId(entity.getSchool().getSchoolId());
        dto.setSchoolName(entity.getSchool().getSchoolName());
        if (entity.getAcademicYear() != null)
            dto.setAcademicYearId(entity.getAcademicYear().getAcademicYearId());
        dto.setStandardId(entity.getAddStandard().getStandardId());
        dto.setStandard(entity.getAddStandard().getStandard());
        dto.setDivisionName(entity.getDivisionName());
        dto.setDescription(entity.getDescription());
        if (entity.getClassTeacher() != null) {
            dto.setClassTeacherId(entity.getClassTeacher().getId());
            dto.setClassTeacherName(entity.getClassTeacher().getTeacherName());
        }
        return dto;
    }

    // ==================== ADD ====================

    @Override
    public AddDivisionDTO addDivision(AddDivisionDTO dto) {
        School school = schoolRepository.findById(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School Not Found"));
        AddStandard standard = standardRepository.findById(dto.getStandardId())
                .orElseThrow(() -> new RuntimeException("Standard Not Found"));

        AddDivision division = new AddDivision();
        division.setSchool(school);
        division.setAddStandard(standard);

        if (dto.getAcademicYearId() != null) {
            AcademicYear ay = academicYearRepository.findById(dto.getAcademicYearId())
                    .orElseThrow(() -> new RuntimeException("Academic Year Not Found"));
            if (!ay.getSchool().getSchoolId().equals(dto.getSchoolId()))
                throw new RuntimeException("Academic Year does not belong to the given School");
            division.setAcademicYear(ay);
        }

        division.setDivisionName(dto.getDivisionName());
        division.setDescription(dto.getDescription());

        // Assign class teacher if provided
        if (dto.getClassTeacherId() != null) {
            AddTeacher teacher = teacherRepository.findById(dto.getClassTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher Not Found"));
            if (!teacher.getSchool().getSchoolId().equals(dto.getSchoolId()))
                throw new RuntimeException("Teacher does not belong to the given School");
            division.setClassTeacher(teacher);
        }

        return mapToDTO(repository.save(division));
    }

    // ==================== GET ALL BY SCHOOL ====================

    @Override
    public List<AddDivisionDTO> getAllBySchoolId(Long schoolId, Long academicYearId) {
        schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School Not Found"));
        AcademicYear ay = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new RuntimeException("Academic Year Not Found"));
        if (!ay.getSchool().getSchoolId().equals(schoolId))
            throw new RuntimeException("Academic Year does not belong to the given School");
        return repository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId)
                .stream().map(this::mapToDTO).toList();
    }

    // ==================== GET ALL BY STANDARD ====================

    @Override
    public List<AddDivisionDTO> getAllByStandardId(Long standardId, Long academicYearId) {
        academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new RuntimeException("Academic Year Not Found"));
        return repository.findByAddStandard_StandardIdAndAcademicYear_AcademicYearId(standardId, academicYearId)
                .stream().map(this::mapToDTO).toList();
    }

    // ==================== GET BY ID ====================

    @Override
    public AddDivisionDTO getByDivisionId(Long divisionId, Long academicYearId) {
        AddDivision division = repository.findByDivisionId(divisionId)
                .orElseThrow(() -> new RuntimeException("Division Not Found"));
        if (academicYearId != null) {
            academicYearRepository.findById(academicYearId)
                    .orElseThrow(() -> new RuntimeException("Academic Year Not Found"));
            if (division.getAcademicYear() == null || !division.getAcademicYear().getAcademicYearId().equals(academicYearId))
                throw new RuntimeException("Division does not belong to the given Academic Year");
        }
        return mapToDTO(division);
    }

    // ==================== UPDATE ====================

    @Override
    public AddDivisionDTO updateDivision(AddDivisionDTO dto) {
        AddDivision existing = repository.findById(dto.getDivisionId())
                .orElseThrow(() -> new RuntimeException("Division Not Found"));

        if (dto.getDivisionName() != null && !dto.getDivisionName().isBlank())
            existing.setDivisionName(dto.getDivisionName());
        existing.setDescription(dto.getDescription());

        // Update class teacher — null removes the assignment
        if (dto.getClassTeacherId() != null) {
            AddTeacher teacher = teacherRepository.findById(dto.getClassTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher Not Found"));
            Long schoolId = existing.getSchool().getSchoolId();
            if (!teacher.getSchool().getSchoolId().equals(schoolId))
                throw new RuntimeException("Teacher does not belong to the given School");
            existing.setClassTeacher(teacher);
        } else {
            existing.setClassTeacher(null);
        }

        return mapToDTO(repository.save(existing));
    }

    // ==================== DELETE ====================

    @Override
    public void deleteDivision(Long divisionId) {
        if (!repository.existsById(divisionId))
            throw new RuntimeException("Division Not Found");
        subjectRepository.deleteAll(subjectRepository.findByAddDivision_DivisionId(divisionId));
        repository.deleteById(divisionId);
    }
}

