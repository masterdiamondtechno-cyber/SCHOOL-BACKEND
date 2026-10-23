package com.schoolmanagement.school.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.schoolmanagement.school.dto.AssignmentDTO;
import com.schoolmanagement.school.entity.AcademicYear;
import com.schoolmanagement.school.entity.AddDivision;
import com.schoolmanagement.school.entity.AddStandard;
import com.schoolmanagement.school.entity.AddSubject;
import com.schoolmanagement.school.entity.AddTeacher;
import com.schoolmanagement.school.entity.Assignment;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddDivisionRepository;
import com.schoolmanagement.school.repository.AddStandardRepository;
import com.schoolmanagement.school.repository.AddSubjectRepository;
import com.schoolmanagement.school.repository.AddTeacherRepository;
import com.schoolmanagement.school.repository.AssignmentRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.repository.StudyGroupRepository;
import com.schoolmanagement.school.service.AssignmentService;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SchoolRepository schoolRepository;
    private final AddStandardRepository standardRepository;
    private final AddDivisionRepository divisionRepository;
    private final AddSubjectRepository subjectRepository;
    private final AddTeacherRepository teacherRepository;
    private final AcademicYearRepository academicYearRepository;
    private final StudyGroupRepository studyGroupRepository;

    public AssignmentServiceImpl(AssignmentRepository assignmentRepository,
                                 SchoolRepository schoolRepository,
                                 AddStandardRepository standardRepository,
                                 AddDivisionRepository divisionRepository,
                                 AddSubjectRepository subjectRepository,
                                 AddTeacherRepository teacherRepository,
                                 AcademicYearRepository academicYearRepository,
                                 StudyGroupRepository studyGroupRepository) {
        this.assignmentRepository = assignmentRepository;
        this.schoolRepository = schoolRepository;
        this.standardRepository = standardRepository;
        this.divisionRepository = divisionRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.academicYearRepository = academicYearRepository;
        this.studyGroupRepository = studyGroupRepository;
    }

    // ==================== ENTITY → DTO CONVERTER ====================
    private AssignmentDTO toDTO(Assignment a) {
        AssignmentDTO dto = new AssignmentDTO();
        dto.setId(a.getId());
        dto.setTitle(a.getTitle());
        dto.setDescription(a.getDescription());
        dto.setDueDate(a.getDueDate());
        dto.setAttachmentUrl(a.getAttachmentUrl());
        dto.setStatus(a.getStatus());
        dto.setCreatedAt(a.getCreatedAt());

        if (a.getSchool() != null) dto.setSchoolId(a.getSchool().getSchoolId());
        if (a.getAcademicYear() != null) dto.setAcademicYearId(a.getAcademicYear().getAcademicYearId());

        if (a.getAddSubject() != null) {
            dto.setSubjectId(a.getAddSubject().getSubjectId());
            dto.setSubjectName(a.getAddSubject().getSubjectName());
        }
        if (a.getAddStandard() != null) {
            dto.setStandardId(a.getAddStandard().getStandardId());
            dto.setStandardName(a.getAddStandard().getStandard());
        }
        if (a.getAddDivision() != null) {
            dto.setDivisionId(a.getAddDivision().getDivisionId());
            dto.setDivisionName(a.getAddDivision().getDivisionName());
        }
        if (a.getAddTeacher() != null) {
            dto.setTeacherId(a.getAddTeacher().getId());
            dto.setTeacherName(a.getAddTeacher().getTeacherName());
        }
        if (a.getStudyGroup() != null) {
            dto.setStudyGroupId(a.getStudyGroup().getId());
            dto.setStudyGroupName(a.getStudyGroup().getGroupName());
        }

        return dto;
    }

    @Override
    public void createAssignment(AssignmentDTO dto) {
        Assignment assignment = new Assignment();

        // 1. Validate school
        School school = schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));
        assignment.setSchool(school);

        // 2. Validate academicYear belongs to school
        if (dto.getAcademicYearId() != null) {
            AcademicYear ay = academicYearRepository.findByAcademicYearIdAndSchool_SchoolId(dto.getAcademicYearId(), dto.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("Academic Year not found for this school"));
            assignment.setAcademicYear(ay);
        }

        // 3. Validate standard belongs to school
        if (dto.getStandardId() != null) {
            AddStandard standard = standardRepository.findByStandardId(dto.getStandardId())
                    .orElseThrow(() -> new RuntimeException("Standard not found"));
            if (!standard.getSchool().getSchoolId().equals(dto.getSchoolId())) {
                throw new RuntimeException("Standard does not belong to this school");
            }
            assignment.setAddStandard(standard);
        }

        // 4. Validate division belongs to school and standard
        if (dto.getDivisionId() != null) {
            AddDivision division = divisionRepository.findByDivisionId(dto.getDivisionId())
                    .orElseThrow(() -> new RuntimeException("Division not found"));
            if (!division.getSchool().getSchoolId().equals(dto.getSchoolId())) {
                throw new RuntimeException("Division does not belong to this school");
            }
            if (dto.getStandardId() != null && !division.getAddStandard().getStandardId().equals(dto.getStandardId())) {
                throw new RuntimeException("Division does not belong to the given standard");
            }
            assignment.setAddDivision(division);
        }

        // 5. Validate subject belongs to school
        if (dto.getSubjectId() != null) {
            AddSubject subject = subjectRepository.findBySubjectId(dto.getSubjectId())
                    .orElseThrow(() -> new RuntimeException("Subject not found"));
            if (!subject.getSchool().getSchoolId().equals(dto.getSchoolId())) {
                throw new RuntimeException("Subject does not belong to this school");
            }
            assignment.setAddSubject(subject);
        }

        // 6. Validate teacher belongs to school
        if (dto.getTeacherId() != null) {
            AddTeacher teacher = teacherRepository.findById(dto.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
            if (!teacher.getSchool().getSchoolId().equals(dto.getSchoolId())) {
                throw new RuntimeException("Teacher does not belong to this school");
            }
            assignment.setAddTeacher(teacher);
        }

        // 7. Validate studyGroup belongs to school (optional)
        if (dto.getStudyGroupId() != null) {
            com.schoolmanagement.school.entity.StudyGroup group = studyGroupRepository.findById(dto.getStudyGroupId())
                    .orElseThrow(() -> new RuntimeException("Study Group not found"));
            if (!group.getSchool().getSchoolId().equals(dto.getSchoolId())) {
                throw new RuntimeException("Study Group does not belong to this school");
            }
            assignment.setStudyGroup(group);
        }

        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setDueDate(dto.getDueDate());
        assignment.setAttachmentUrl(dto.getAttachmentUrl());
        assignment.setStatus("ACTIVE");

        assignmentRepository.save(assignment);
    }

    @Override
    public AssignmentDTO getAssignmentById(Long id, Long schoolId) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        return toDTO(assignment);
    }

    @Override
    public void updateAssignment(AssignmentDTO dto) {
        Assignment assignment = assignmentRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (dto.getTitle() != null) assignment.setTitle(dto.getTitle());
        if (dto.getDescription() != null) assignment.setDescription(dto.getDescription());
        if (dto.getDueDate() != null) assignment.setDueDate(dto.getDueDate());
        if (dto.getAttachmentUrl() != null) assignment.setAttachmentUrl(dto.getAttachmentUrl());
        if (dto.getStatus() != null) assignment.setStatus(dto.getStatus());

        if (dto.getSubjectId() != null) {
            assignment.setAddSubject(subjectRepository.findBySubjectId(dto.getSubjectId()).orElse(null));
        }
        if (dto.getStandardId() != null) {
            assignment.setAddStandard(standardRepository.findByStandardId(dto.getStandardId()).orElse(null));
        }
        if (dto.getDivisionId() != null) {
            assignment.setAddDivision(divisionRepository.findByDivisionId(dto.getDivisionId()).orElse(null));
        }

        assignmentRepository.save(assignment);
    }

    @Override
    public void deleteAssignment(Long id, Long schoolId) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        assignmentRepository.delete(assignment);
    }

    @Override
    public List<AssignmentDTO> getAssignmentsBySchoolAndStandardAndDivision(Long schoolId, Long academicYearId, Long standardId, Long divisionId) {
        return assignmentRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndStatus(
                        schoolId, academicYearId, standardId, divisionId, "ACTIVE")
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<AssignmentDTO> getActiveAssignmentsBySchool(Long schoolId, Long academicYearId) {
        if (academicYearId != null) {
            return assignmentRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(schoolId, academicYearId, "ACTIVE")
                    .stream().map(this::toDTO).collect(Collectors.toList());
        }
        return assignmentRepository.findBySchool_SchoolIdAndStatus(schoolId, "ACTIVE")
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<AssignmentDTO> getAssignmentsByTeacher(Long schoolId, Long teacherId) {
        return assignmentRepository.findBySchool_SchoolIdAndAddTeacher_IdAndStatus(schoolId, teacherId, "ACTIVE")
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public void closeAssignment(Long id, Long schoolId) {
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        assignment.setStatus("CLOSED");
        assignmentRepository.save(assignment);
    }
}
