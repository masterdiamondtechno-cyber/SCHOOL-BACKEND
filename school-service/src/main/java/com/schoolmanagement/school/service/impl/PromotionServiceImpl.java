package com.schoolmanagement.school.serviceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.schoolmanagement.school.dto.PromotionRequestDTO;
import com.schoolmanagement.school.entity.*;
import com.schoolmanagement.school.repository.*;
import com.schoolmanagement.school.service.PromotionService;

@Service
public class PromotionServiceImpl implements PromotionService {

    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AddStandardRepository standardRepository;
    private final AddDivisionRepository divisionRepository;
    private final AddStudentRepository studentRepository;
    private final StudentAcademicYearRepository studentAcademicYearRepository;

    public PromotionServiceImpl(SchoolRepository schoolRepository,
                                AcademicYearRepository academicYearRepository,
                                AddStandardRepository standardRepository,
                                AddDivisionRepository divisionRepository,
                                AddStudentRepository studentRepository,
                                StudentAcademicYearRepository studentAcademicYearRepository) {
        this.schoolRepository = schoolRepository;
        this.academicYearRepository = academicYearRepository;
        this.standardRepository = standardRepository;
        this.divisionRepository = divisionRepository;
        this.studentRepository = studentRepository;
        this.studentAcademicYearRepository = studentAcademicYearRepository;
    }

    @Override
    @Transactional
    public void promoteStudents(PromotionRequestDTO dto) {
        // Validate school exists
        School school = schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));

        // Validate source academic year belongs to this school
        AcademicYear fromYear = academicYearRepository
                .findByAcademicYearIdAndSchool_SchoolId(dto.getFromAcademicYearId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Source Academic Year not found for this school"));

        // Validate target academic year belongs to this school
        AcademicYear toYear = academicYearRepository
                .findByAcademicYearIdAndSchool_SchoolId(dto.getToAcademicYearId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Target Academic Year not found for this school"));

        if ("ARCHIVED".equalsIgnoreCase(toYear.getStatus())) {
            throw new RuntimeException("Target Academic Year is archived. Cannot promote students into it.");
        }

        // Validate source standard and division
        AddStandard fromStd = standardRepository.findByStandardId(dto.getFromStandardId())
                .orElseThrow(() -> new RuntimeException("Source Standard not found"));
        AddDivision fromDiv = divisionRepository.findByDivisionId(dto.getFromDivisionId())
                .orElseThrow(() -> new RuntimeException("Source Division not found"));

        // Validate target standard and division
        AddStandard toStd = standardRepository.findByStandardId(dto.getToStandardId())
                .orElseThrow(() -> new RuntimeException("Target Standard not found"));
        AddDivision toDiv = divisionRepository.findByDivisionId(dto.getToDivisionId())
                .orElseThrow(() -> new RuntimeException("Target Division not found"));

        if (dto.getStudents() == null || dto.getStudents().isEmpty()) {
            throw new RuntimeException("No student records provided for promotion");
        }

        for (PromotionRequestDTO.StudentPromotionEntryDTO entry : dto.getStudents()) {
            AddStudent student = studentRepository.findByIdAndSchool_SchoolId(entry.getStudentId(), dto.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("Student not found for id: " + entry.getStudentId()));

            // Resolve and update source year mapping
            StudentAcademicYear fromMapping = studentAcademicYearRepository
                    .findByStudent_IdAndAcademicYear_AcademicYearId(student.getId(), dto.getFromAcademicYearId())
                    .orElse(null);

            String status = entry.getPromotionStatus() != null ? entry.getPromotionStatus() : "PROMOTED";
            if (fromMapping != null) {
                fromMapping.setStatus(status);
                studentAcademicYearRepository.save(fromMapping);
            }

            // Create/update target year mapping
            StudentAcademicYear toMapping = studentAcademicYearRepository
                    .findByStudent_IdAndAcademicYear_AcademicYearId(student.getId(), dto.getToAcademicYearId())
                    .orElse(new StudentAcademicYear());

            toMapping.setStudent(student);
            toMapping.setAcademicYear(toYear);

            if ("DETAINED".equalsIgnoreCase(status)) {
                toMapping.setStandard(fromStd);
                toMapping.setDivision(fromDiv);
            } else {
                toMapping.setStandard(toStd);
                toMapping.setDivision(toDiv);
            }

            String newRoll = entry.getNewRollNo() != null ? entry.getNewRollNo() 
                    : (fromMapping != null ? fromMapping.getRollNo() : null);
            toMapping.setRollNo(newRoll);
            toMapping.setStatus("ACTIVE");
            studentAcademicYearRepository.save(toMapping);

            // Synchronize legacy compatibility fields if target year is the current active year
            if (toYear.getIsCurrent() != null && toYear.getIsCurrent()) {
                if ("DETAINED".equalsIgnoreCase(status)) {
                    student.setAddStandard(fromStd);
                    student.setAddDivision(fromDiv);
                } else {
                    student.setAddStandard(toStd);
                    student.setAddDivision(toDiv);
                }
                if (newRoll != null) {
                    student.setRollNumber(newRoll);
                }
                studentRepository.save(student);
            }
        }
    }
}
