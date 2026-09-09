package com.schoolmanagement.school_admin_ServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.schoolmanagement.school_admin_DTO.ExamDTO;
import com.schoolmanagement.school_admin_DTO.ExamMarksDTO;
import com.schoolmanagement.school_admin_DTO.HallTicketDTO;
import com.schoolmanagement.school_admin_Entity.*;
import com.schoolmanagement.school_admin_Repo.*;
import com.schoolmanagement.school_admin_Service.ExamService;

@Service
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final ExamMarksRepository examMarksRepository;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AddStandardRepository standardRepository;
    private final AddDivisionRepository divisionRepository;
    private final AddStudentRepository studentRepository;
    private final AddSubjectRepository subjectRepository;
    private final StudentAcademicYearRepository studentAcademicYearRepository;

    public ExamServiceImpl(ExamRepository examRepository, ExamMarksRepository examMarksRepository,
                           SchoolRepository schoolRepository, AcademicYearRepository academicYearRepository,
                           AddStandardRepository standardRepository, AddDivisionRepository divisionRepository,
                           AddStudentRepository studentRepository, AddSubjectRepository subjectRepository,
                           StudentAcademicYearRepository studentAcademicYearRepository) {
        this.examRepository = examRepository;
        this.examMarksRepository = examMarksRepository;
        this.schoolRepository = schoolRepository;
        this.academicYearRepository = academicYearRepository;
        this.standardRepository = standardRepository;
        this.divisionRepository = divisionRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.studentAcademicYearRepository = studentAcademicYearRepository;
    }

    // ==================== ENTITY → DTO HELPERS ====================

    private ExamDTO toExamDTO(Exam exam) {
        ExamDTO dto = new ExamDTO();
        dto.setExamId(exam.getExamId());
        dto.setSchoolId(exam.getSchool().getSchoolId());
        if (exam.getAcademicYear() != null)
            dto.setAcademicYearId(exam.getAcademicYear().getAcademicYearId());
        dto.setStandardId(exam.getAddStandard().getStandardId());
        dto.setDivisionId(exam.getAddDivision().getDivisionId());
        if (exam.getAddSubject() != null) {
            dto.setSubjectId(exam.getAddSubject().getSubjectId());
            dto.setSubjectName(exam.getAddSubject().getSubjectName());
        }
        dto.setExamName(exam.getExamName());
        dto.setExamMode(exam.getExamMode());
        dto.setExamDate(exam.getExamDate());
        dto.setDuration(exam.getDuration());
        dto.setTotalMarks(exam.getTotalMarks());
        dto.setStatus(exam.getStatus());
        return dto;
    }

    private ExamMarksDTO toExamMarksDTO(ExamMarks marks) {
        ExamMarksDTO dto = new ExamMarksDTO();
        dto.setExamMarksId(marks.getExamMarksId());
        dto.setSchoolId(marks.getSchool().getSchoolId());
        if (marks.getAcademicYear() != null)
            dto.setAcademicYearId(marks.getAcademicYear().getAcademicYearId());
        dto.setExamId(marks.getExam().getExamId());
        dto.setStudentId(marks.getAddStudent().getId());
        dto.setSubjectId(marks.getAddSubject().getSubjectId());
        dto.setMarksObtained(marks.getMarksObtained());
        dto.setMaxMarks(marks.getMaxMarks());
        dto.setGrade(marks.getGrade());
        dto.setRemark(marks.getRemark());
        dto.setIsAbsent(marks.getIsAbsent());
        return dto;
    }

    // ==================== CRUD ====================

    @Override
    public void createExam(ExamDTO dto) {
        // 1. Validate school exists
        School school = schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));

        // 2. Validate academicYear belongs to this school
        AcademicYear academicYear = academicYearRepository
                .findByAcademicYearIdAndSchool_SchoolId(dto.getAcademicYearId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Academic Year not found for this school"));

        // 3. Validate standard belongs to this school
        AddStandard standard = standardRepository.findByStandardId(dto.getStandardId())
                .orElseThrow(() -> new RuntimeException("Standard not found"));
        if (!standard.getSchool().getSchoolId().equals(dto.getSchoolId())) {
            throw new RuntimeException("Standard does not belong to this school");
        }

        // 4. Validate division belongs to this school and standard
        AddDivision division = divisionRepository.findByDivisionId(dto.getDivisionId())
                .orElseThrow(() -> new RuntimeException("Division not found"));
        if (!division.getSchool().getSchoolId().equals(dto.getSchoolId())) {
            throw new RuntimeException("Division does not belong to this school");
        }
        if (!division.getAddStandard().getStandardId().equals(dto.getStandardId())) {
            throw new RuntimeException("Division does not belong to the given standard");
        }

        // 5. Validate subject belongs to this school, standard, and division
        AddSubject subject = subjectRepository.findBySubjectId(dto.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        if (!subject.getSchool().getSchoolId().equals(dto.getSchoolId())) {
            throw new RuntimeException("Subject does not belong to this school");
        }

        Exam exam = new Exam();
        exam.setSchool(school);
        exam.setAcademicYear(academicYear);
        exam.setAddStandard(standard);
        exam.setAddDivision(division);
        exam.setAddSubject(subject);
        exam.setExamName(dto.getExamName());
        exam.setExamMode(dto.getExamMode());
        exam.setExamDate(dto.getExamDate());
        exam.setDuration(dto.getDuration());
        exam.setTotalMarks(dto.getTotalMarks());
        exam.setStatus(dto.getStatus() != null ? dto.getStatus() : "UPCOMING");
        examRepository.save(exam);
    }

    @Override
    public List<ExamDTO> getAllBySchoolId(Long schoolId, Long academicYearId) {
        return examRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId)
                .stream().map(this::toExamDTO).collect(Collectors.toList());
    }

    @Override
    public List<ExamDTO> getByDivision(Long schoolId, Long standardId, Long divisionId, Long academicYearId) {
        return examRepository.findBySchool_SchoolIdAndAddStandard_StandardIdAndAddDivision_DivisionIdAndAcademicYear_AcademicYearId(
                schoolId, standardId, divisionId, academicYearId)
                .stream().map(this::toExamDTO).collect(Collectors.toList());
    }

    @Override
    public ExamDTO getById(Long examId, Long schoolId) {
        Exam exam = examRepository.findByExamIdAndSchool_SchoolId(examId, schoolId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        return toExamDTO(exam);
    }

    @Override
    public void updateExam(ExamDTO dto) {
        Exam exam = examRepository.findByExamIdAndSchool_SchoolId(dto.getExamId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        if (dto.getExamName() != null) exam.setExamName(dto.getExamName());
        if (dto.getExamMode() != null) exam.setExamMode(dto.getExamMode());
        if (dto.getExamDate() != null) exam.setExamDate(dto.getExamDate());
        if (dto.getDuration() != null) exam.setDuration(dto.getDuration());
        if (dto.getTotalMarks() != null) exam.setTotalMarks(dto.getTotalMarks());
        if (dto.getStatus() != null) exam.setStatus(dto.getStatus());
        if (dto.getSubjectId() != null) {
            exam.setAddSubject(subjectRepository.findBySubjectId(dto.getSubjectId())
                    .orElseThrow(() -> new RuntimeException("Subject not found")));
        }
        examRepository.save(exam);
    }

    @Override
    @Transactional
    public void deleteExam(Long examId, Long schoolId) {
        Exam exam = examRepository.findByExamIdAndSchool_SchoolId(examId, schoolId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        // Delete associated marks first to avoid FK constraint violation
        examMarksRepository.deleteByExam_ExamId(examId);
        examRepository.delete(exam);
    }

    // ==================== MARKS ====================

    @Override
    public void saveMarksBulk(ExamMarksDTO dto) {
        // 1. Validate school exists
        School school = schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));

        // 2. Validate exam belongs to this school
        Exam exam = examRepository.findByExamIdAndSchool_SchoolId(dto.getExamId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Exam not found for this school"));

        // 3. Validate academicYear belongs to this school
        AcademicYear academicYear = academicYearRepository
                .findByAcademicYearIdAndSchool_SchoolId(dto.getAcademicYearId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Academic Year not found for this school"));

        // 4. Validate subject belongs to this school
        AddSubject subject = subjectRepository.findBySubjectId(dto.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        if (!subject.getSchool().getSchoolId().equals(dto.getSchoolId())) {
            throw new RuntimeException("Subject does not belong to this school");
        }

        for (ExamMarksDTO.ExamMarksEntryDTO entry : dto.getEntries()) {
            // 5. Validate each student belongs to this school
            AddStudent student = studentRepository.findById(entry.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found: " + entry.getStudentId()));
            if (!student.getSchool().getSchoolId().equals(dto.getSchoolId())) {
                throw new RuntimeException("Student " + entry.getStudentId() + " does not belong to this school");
            }

            ExamMarks marks = examMarksRepository
                    .findByExam_ExamIdAndAddStudent_IdAndAddSubject_SubjectId(
                            dto.getExamId(), entry.getStudentId(), dto.getSubjectId())
                    .orElse(new ExamMarks());

            marks.setSchool(school);
            marks.setExam(exam);
            marks.setAcademicYear(academicYear);
            marks.setAddSubject(subject);
            marks.setAddStudent(student);
            marks.setMarksObtained(entry.getMarksObtained());
            marks.setMaxMarks(exam.getTotalMarks());
            marks.setIsAbsent(entry.getIsAbsent() != null ? entry.getIsAbsent() : false);
            marks.setRemark(entry.getRemark());

            // Auto-calculate grade
            if (!marks.getIsAbsent() && entry.getMarksObtained() != null && exam.getTotalMarks() != null) {
                double pct = (entry.getMarksObtained() / exam.getTotalMarks()) * 100;
                marks.setGrade(calculateGrade(pct));
            }

            examMarksRepository.save(marks);
        }
    }

    private String calculateGrade(double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B+";
        if (percentage >= 60) return "B";
        if (percentage >= 50) return "C";
        if (percentage >= 35) return "D";
        return "FAIL";
    }

    @Override
    public List<ExamMarksDTO> getMarksByExam(Long examId, Long schoolId, Long academicYearId) {
        return examMarksRepository.findByExam_ExamIdAndSchool_SchoolIdAndAcademicYear_AcademicYearId(examId, schoolId, academicYearId)
                .stream().map(this::toExamMarksDTO).collect(Collectors.toList());
    }

    @Override
    public List<ExamMarksDTO> getMarksByStudent(Long examId, Long studentId, Long schoolId, Long academicYearId) {
        if (examId != null) {
            return examMarksRepository.findByExam_ExamIdAndAddStudent_IdAndSchool_SchoolIdAndAcademicYear_AcademicYearId(
                    examId, studentId, schoolId, academicYearId)
                    .stream().map(this::toExamMarksDTO).collect(Collectors.toList());
        }
        return examMarksRepository.findByAddStudent_IdAndSchool_SchoolIdAndAcademicYear_AcademicYearId(studentId, schoolId, academicYearId)
                .stream().map(this::toExamMarksDTO).collect(Collectors.toList());
    }

    // ==================== STUDENT-FACING APIs ====================

    @Override
    public List<ExamDTO> getExamsByStudent(Long schoolId, Long academicYearId, Long studentId) {
        AddStudent student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Optional<StudentAcademicYear> mapping = studentAcademicYearRepository
                .findByStudent_IdAndAcademicYear_AcademicYearId(studentId, academicYearId);
        if (mapping.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        Long standardId = mapping.get().getStandard() != null ? mapping.get().getStandard().getStandardId() : null;
        Long divisionId = mapping.get().getDivision() != null ? mapping.get().getDivision().getDivisionId() : null;

        List<Exam> exams = examRepository
                .findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndAddStandard_StandardIdAndAddDivision_DivisionId(
                        schoolId, academicYearId, standardId, divisionId);

        return exams.stream().map(exam -> {
            ExamDTO dto = toExamDTO(exam);

            // For COMPLETED exams, fetch marks for this student
            if ("COMPLETED".equalsIgnoreCase(exam.getStatus())) {
                examMarksRepository.findByExam_ExamIdAndAddStudent_IdAndAddSubject_SubjectId(
                        exam.getExamId(), studentId,
                        exam.getAddSubject() != null ? exam.getAddSubject().getSubjectId() : null)
                        .ifPresent(marks -> {
                            dto.setMarksObtained(marks.getMarksObtained());
                            if (marks.getMarksObtained() != null && marks.getMaxMarks() != null) {
                                dto.setScore(marks.getMarksObtained().intValue() + "/" + marks.getMaxMarks().intValue());
                            }
                        });
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public HallTicketDTO getHallTicket(Long schoolId, Long academicYearId, Long studentId, Long examId) {
        AddStudent student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Exam exam = examRepository.findByExamIdAndSchool_SchoolId(examId, schoolId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        Optional<StudentAcademicYear> mapping = studentAcademicYearRepository
                .findByStudent_IdAndAcademicYear_AcademicYearId(studentId, academicYearId);
        String standardName = (mapping.isPresent() && mapping.get().getStandard() != null) ? mapping.get().getStandard().getStandard() 
                : (student.getAddStandard() != null ? student.getAddStandard().getStandard() : "");
        String divisionName = (mapping.isPresent() && mapping.get().getDivision() != null) ? mapping.get().getDivision().getDivisionName() 
                : (student.getAddDivision() != null ? student.getAddDivision().getDivisionName() : "");
 
        HallTicketDTO dto = new HallTicketDTO();
        dto.setStudentName(student.getFirstName()
                + (student.getMiddleName() != null ? " " + student.getMiddleName() : "")
                + (student.getLastName() != null ? " " + student.getLastName() : ""));
        dto.setStudentId(student.getStudentId());
        dto.setStandardName(standardName);
        dto.setDivisionName(divisionName);
        dto.setSchoolName(exam.getSchool().getSchoolName());
        dto.setExamName(exam.getExamName());
        if (exam.getAddSubject() != null) {
            dto.setSubjectName(exam.getAddSubject().getSubjectName());
        }
        dto.setExamMode(exam.getExamMode());
        dto.setExamDate(exam.getExamDate());
        dto.setDuration(exam.getDuration());
        dto.setTotalMarks(exam.getTotalMarks());
        dto.setStatus(exam.getStatus());

        return dto;
    }
}

