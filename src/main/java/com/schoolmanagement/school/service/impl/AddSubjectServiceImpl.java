package com.schoolmanagement.school.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.schoolmanagement.school.dto.AddSubjectDTO;
import com.schoolmanagement.school.entity.AcademicYear;
import com.schoolmanagement.school.entity.AddDivision;
import com.schoolmanagement.school.entity.AddStandard;
import com.schoolmanagement.school.entity.AddSubject;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddDivisionRepository;
import com.schoolmanagement.school.repository.AddStandardRepository;
import com.schoolmanagement.school.repository.AddSubjectRepository;
import com.schoolmanagement.school.repository.AddTeacherRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.service.AddSubjectService;

@Service
public class AddSubjectServiceImpl implements AddSubjectService {

    @Autowired
    private AddSubjectRepository repository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private AddStandardRepository standardRepository;

    @Autowired
    private AddDivisionRepository divisionRepository;

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private AddTeacherRepository teacherRepository;

    @Override
    public List<AddSubjectDTO> getAllBySchoolId(Long schoolId, Long academicYearId) {
        List<AddSubject> list = repository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId);
        return list.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private AddSubjectDTO mapToDTO(AddSubject entity) {
        AddSubjectDTO dto = new AddSubjectDTO();
        dto.setSubjectId(entity.getSubjectId());
        dto.setSchoolId(entity.getSchool().getSchoolId());
        dto.setSchoolName(entity.getSchool().getSchoolName());
        if (entity.getAcademicYear() != null) {
            dto.setAcademicYearId(entity.getAcademicYear().getAcademicYearId());
            dto.setAcademicYearName(entity.getAcademicYear().getYearName());
        }
        dto.setStandardId(entity.getAddStandard().getStandardId());
        dto.setStandard(entity.getAddStandard().getStandard());
        dto.setDivisionId(entity.getAddDivision().getDivisionId());
        dto.setDivisionName(entity.getAddDivision().getDivisionName());
        dto.setSubjectName(entity.getSubjectName());
        dto.setDescription(entity.getDescription());
        
        if (entity.getAddTeacher() != null) {
            dto.setTeacherId(entity.getAddTeacher().getId());
            dto.setTeacherName(entity.getAddTeacher().getTeacherName());
        }
        
        return dto;
    }

    @Override
    public AddSubjectDTO addSubject(AddSubjectDTO dto) {
        School school = schoolRepository.findById(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School Not Found"));
        AddStandard standard = standardRepository.findById(dto.getStandardId())
                .orElseThrow(() -> new RuntimeException("Standard Not Found"));
        AddDivision division = divisionRepository.findById(dto.getDivisionId())
                .orElseThrow(() -> new RuntimeException("Division Not Found"));

        AddSubject subject = new AddSubject();
        subject.setSchool(school);
        subject.setAddStandard(standard);
        subject.setAddDivision(division);
        if (dto.getAcademicYearId() != null) {
            AcademicYear ay = academicYearRepository.findById(dto.getAcademicYearId())
                    .orElseThrow(() -> new RuntimeException("Academic Year Not Found"));
            // Check association: academicYear must belong to the same school
            if (!ay.getSchool().getSchoolId().equals(dto.getSchoolId())) {
                throw new RuntimeException("Academic Year does not belong to the given School");
            }
            subject.setAcademicYear(ay);
        }
        
        if (dto.getTeacherId() != null) {
            com.schoolmanagement.school.entity.AddTeacher teacher = teacherRepository.findById(dto.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher Not Found"));
            // Ensure teacher belongs to the same school
            if (!teacher.getSchool().getSchoolId().equals(dto.getSchoolId())) {
                throw new RuntimeException("Teacher does not belong to the given School");
            }
            subject.setAddTeacher(teacher);
        }
        
        subject.setSubjectName(dto.getSubjectName());
        subject.setDescription(dto.getDescription());

        return mapToDTO(repository.save(subject));
    }

    @Override
    public List<AddSubjectDTO> getAllBySchoolStandardDivision(
            Long schoolId,
            Long standardId,
            Long divisionId,
            Long academicYearId
    ) {
        // Check school exists
        schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School Not Found"));
        // Check academic year exists and belongs to school
        AcademicYear ay = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new RuntimeException("Academic Year Not Found"));
        if (!ay.getSchool().getSchoolId().equals(schoolId)) {
            throw new RuntimeException("Academic Year does not belong to the given School");
        }
        return repository
                .findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndAcademicYear_AcademicYearId(
                        schoolId,
                        standardId,
                        divisionId,
                        academicYearId
                )
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AddSubjectDTO getBySubjectId(Long subjectId, Long academicYearId) {
        AddSubject subject = repository.findBySubjectId(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject Not Found"));
        // If academicYearId is provided, validate it
        if (academicYearId != null) {
            academicYearRepository.findById(academicYearId)
                    .orElseThrow(() -> new RuntimeException("Academic Year Not Found"));
            if (subject.getAcademicYear() == null || !subject.getAcademicYear().getAcademicYearId().equals(academicYearId)) {
                throw new RuntimeException("Subject does not belong to the given Academic Year");
            }
        }
        return mapToDTO(subject);
    }

    @Override
    public AddSubjectDTO updateSubject(AddSubjectDTO dto) {
        AddSubject existing = repository.findById(dto.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject Not Found"));

        if (dto.getSchoolId() != null) {
            School school = schoolRepository.findById(dto.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("School Not Found"));
            existing.setSchool(school);
        }
        if (dto.getStandardId() != null) {
            AddStandard standard = standardRepository.findById(dto.getStandardId())
                    .orElseThrow(() -> new RuntimeException("Standard Not Found"));
            existing.setAddStandard(standard);
        }
        if (dto.getDivisionId() != null) {
            AddDivision division = divisionRepository.findById(dto.getDivisionId())
                    .orElseThrow(() -> new RuntimeException("Division Not Found"));
            existing.setAddDivision(division);
        }
        if (dto.getTeacherId() != null) {
            com.schoolmanagement.school.entity.AddTeacher teacher = teacherRepository.findById(dto.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher Not Found"));
            // Ensure teacher belongs to the same school
            if (!teacher.getSchool().getSchoolId().equals(existing.getSchool().getSchoolId())) {
                throw new RuntimeException("Teacher does not belong to the given School");
            }
            existing.setAddTeacher(teacher);
        } else if (dto.getTeacherId() == null && dto.getSubjectName() != null) {
        	// We only clear if the request explicitly asks to update but sends null.
        	// Standard update logic usually only updates non-null fields. 
        	// Alternatively, we don't clear. Let's keep it non-null updates only unless requested.
        }
        
        if (dto.getSubjectName() != null) existing.setSubjectName(dto.getSubjectName());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());

        return mapToDTO(repository.save(existing));
    }

    @Override
    public void deleteSubject(Long subjectId) {
        if (!repository.existsById(subjectId)) {
            throw new RuntimeException("Subject Not Found");
        }
        repository.deleteById(subjectId);
    }

    @Override
    public List<AddSubjectDTO> getByTeacherId(Long teacherId, Long academicYearId) {
        // Find teacher to ensure exists
        teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher Not Found"));
        
        // Ensure academic year exists
        academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new RuntimeException("Academic Year Not Found"));

        List<AddSubject> list = repository.findByAddTeacher_IdAndAcademicYear_AcademicYearId(teacherId, academicYearId);
        return list.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
}

