package com.schoolmanagement.school.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.schoolmanagement.school.dto.AddStandardDTO;
import com.schoolmanagement.school.dto.SectionSummaryDTO;
import com.schoolmanagement.school.entity.AcademicYear;
import com.schoolmanagement.school.entity.AddStandard;
import com.schoolmanagement.school.entity.AddTeacher;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddDivisionRepository;
import com.schoolmanagement.school.repository.AddStandardRepository;
import com.schoolmanagement.school.repository.AddSubjectRepository;
import com.schoolmanagement.school.repository.AddTeacherRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.service.AddStandardService;

@Service
public class AddStandardServiceImpl implements AddStandardService {

    @Autowired
    private AddStandardRepository repository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private AddDivisionRepository divisionRepository;

    @Autowired
    private AddSubjectRepository subjectRepository;

    @Autowired
    private AddTeacherRepository teacherRepository;

    private AddStandardDTO mapToDTO(AddStandard entity) {
        AddStandardDTO dto = new AddStandardDTO();
        dto.setStandardId(entity.getStandardId());
        dto.setSchoolId(entity.getSchool().getSchoolId());
        dto.setSchoolName(entity.getSchool().getSchoolName());
        if (entity.getAcademicYear() != null)
            dto.setAcademicYearId(entity.getAcademicYear().getAcademicYearId());
        dto.setStandard(entity.getStandard());
        dto.setDescription(entity.getDescription());
        if (entity.getClassTeacher() != null) {
            dto.setClassTeacherId(entity.getClassTeacher().getId());
            dto.setClassTeacherName(entity.getClassTeacher().getTeacherName());
        }

        // Fetch divisions for this standard and academic year
        List<SectionSummaryDTO> sectionList = divisionRepository
                .findByAddStandard_StandardIdAndAcademicYear_AcademicYearId(entity.getStandardId(),
                        entity.getAcademicYear() != null ? entity.getAcademicYear().getAcademicYearId() : null)
                .stream()
                .map(div -> new SectionSummaryDTO(
                        div.getDivisionId(),
                        div.getDivisionName(),
                        div.getClassTeacher() != null ? div.getClassTeacher().getId() : null,
                        div.getClassTeacher() != null ? div.getClassTeacher().getTeacherName() : null))
                .toList();
        
        dto.setSections(sectionList);
        dto.setSectionCount(sectionList.size());

        return dto;
    }

    @Override
    public AddStandardDTO addStandard(AddStandardDTO dto) {
        School school = schoolRepository.findById(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School Not Found"));

        AddStandard entity = new AddStandard();
        entity.setSchool(school);
        if (dto.getAcademicYearId() != null) {
            AcademicYear ay = academicYearRepository.findById(dto.getAcademicYearId())
                    .orElseThrow(() -> new RuntimeException("Academic Year Not Found"));
            // Check association: academicYear must belong to the same school
            if (!ay.getSchool().getSchoolId().equals(dto.getSchoolId())) {
                throw new RuntimeException("Academic Year does not belong to the given School");
            }
            entity.setAcademicYear(ay);
        }
        entity.setStandard(dto.getStandard());
        entity.setDescription(dto.getDescription());

        // Set class teacher if provided
        if (dto.getClassTeacherId() != null) {
            AddTeacher teacher = teacherRepository.findById(dto.getClassTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher Not Found"));
            if (!teacher.getSchool().getSchoolId().equals(dto.getSchoolId())) {
                throw new RuntimeException("Teacher does not belong to the given School");
            }
            entity.setClassTeacher(teacher);
        }

        AddStandard saved = repository.save(entity);
        return mapToDTO(saved);
    }

    @Override
    public List<AddStandardDTO> getAllBySchoolId(Long schoolId, Long academicYearId) {
        // Check school exists
        schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School Not Found"));
        // Check academic year exists and belongs to the school
        AcademicYear ay = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new RuntimeException("Academic Year Not Found"));
        if (!ay.getSchool().getSchoolId().equals(schoolId)) {
            throw new RuntimeException("Academic Year does not belong to the given School");
        }
        return repository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public AddStandardDTO getByStandardId(Long standardId, Long academicYearId) {
        AddStandard entity = repository.findByStandardId(standardId)
                .orElseThrow(() -> new RuntimeException("Standard Not Found"));
        // If academicYearId is provided, validate it
        if (academicYearId != null) {
            academicYearRepository.findById(academicYearId)
                    .orElseThrow(() -> new RuntimeException("Academic Year Not Found"));
            if (entity.getAcademicYear() == null || !entity.getAcademicYear().getAcademicYearId().equals(academicYearId)) {
                throw new RuntimeException("Standard does not belong to the given Academic Year");
            }
        }
        return mapToDTO(entity);
    }

    @Override
    public AddStandardDTO updateStandard(AddStandardDTO dto) {
        AddStandard existing = repository.findById(dto.getStandardId())
                .orElseThrow(() -> new RuntimeException("Standard Not Found"));

        if (dto.getSchoolId() != null) {
            School school = schoolRepository.findById(dto.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("School Not Found"));
            existing.setSchool(school);
        }
        existing.setStandard(dto.getStandard());
        existing.setDescription(dto.getDescription());

        // Update class teacher
        if (dto.getClassTeacherId() != null) {
            AddTeacher teacher = teacherRepository.findById(dto.getClassTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher Not Found"));
            if (!teacher.getSchool().getSchoolId().equals(
                    dto.getSchoolId() != null ? dto.getSchoolId() : existing.getSchool().getSchoolId())) {
                throw new RuntimeException("Teacher does not belong to the given School");
            }
            existing.setClassTeacher(teacher);
        } else {
            // Allow removing the class teacher by passing null
            existing.setClassTeacher(null);
        }

        return mapToDTO(repository.save(existing));
    }

    @Override
    public void deleteStandard(Long standardId) {
        if (!repository.existsById(standardId)) {
            throw new RuntimeException("Standard Not Found");
        }
        // Delete all subjects under this standard first
        subjectRepository.deleteAll(subjectRepository.findByAddStandard_StandardId(standardId));
        // Delete all divisions under this standard
        divisionRepository.deleteAll(divisionRepository.findByAddStandard_StandardId(standardId));
        // Now delete the standard
        repository.deleteById(standardId);
    }
}
