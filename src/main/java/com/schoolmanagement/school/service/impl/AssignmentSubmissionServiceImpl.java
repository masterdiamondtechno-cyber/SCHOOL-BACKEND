package com.schoolmanagement.school.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.schoolmanagement.school.dto.AssignmentSubmissionDTO;
import com.schoolmanagement.school.entity.AddStudent;
import com.schoolmanagement.school.entity.Assignment;
import com.schoolmanagement.school.entity.AssignmentSubmission;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.repository.AddStudentRepository;
import com.schoolmanagement.school.repository.AssignmentRepository;
import com.schoolmanagement.school.repository.AssignmentSubmissionRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.service.AssignmentSubmissionService;

@Service
public class AssignmentSubmissionServiceImpl implements AssignmentSubmissionService {

    private final AssignmentSubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final AddStudentRepository studentRepository;
    private final SchoolRepository schoolRepository;

    public AssignmentSubmissionServiceImpl(AssignmentSubmissionRepository submissionRepository,
                                           AssignmentRepository assignmentRepository,
                                           AddStudentRepository studentRepository,
                                           SchoolRepository schoolRepository) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.studentRepository = studentRepository;
        this.schoolRepository = schoolRepository;
    }

    // ==================== ENTITY → DTO CONVERTER ====================
    private AssignmentSubmissionDTO toDTO(AssignmentSubmission s) {
        AssignmentSubmissionDTO dto = new AssignmentSubmissionDTO();
        dto.setId(s.getId());
        dto.setFileUrl(s.getFileUrl());
        dto.setSubmissionDate(s.getSubmissionDate());
        dto.setStatus(s.getStatus());
        dto.setRemarks(s.getRemarks());
        dto.setMarks(s.getMarks());
        dto.setCreatedAt(s.getCreatedAt());

        if (s.getAssignment() != null) {
            dto.setAssignmentId(s.getAssignment().getId());
            dto.setAssignmentTitle(s.getAssignment().getTitle());
        }
        if (s.getStudent() != null) {
            dto.setStudentId(s.getStudent().getId());
            dto.setStudentName(s.getStudent().getFirstName() + " " + s.getStudent().getLastName());
        }
        if (s.getSchool() != null) {
            dto.setSchoolId(s.getSchool().getSchoolId());
        }

        return dto;
    }

    // ==================== SUBMIT (Student uploads before deadline) ====================
    @Override
    public void submitAssignment(AssignmentSubmissionDTO dto) {
        // 1. Validate school
        School school = schoolRepository.findBySchoolId(dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));

        // 2. Validate assignment belongs to school
        Assignment assignment = assignmentRepository.findById(dto.getAssignmentId())
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        if (!assignment.getSchool().getSchoolId().equals(dto.getSchoolId())) {
            throw new RuntimeException("Assignment does not belong to this school");
        }

        // 3. Check assignment is active
        if ("CLOSED".equalsIgnoreCase(assignment.getStatus())) {
            throw new RuntimeException("Assignment is closed, submissions not allowed");
        }

        // 4. Check deadline — student cannot submit after dueDate
        if (assignment.getDueDate() != null && LocalDate.now().isAfter(assignment.getDueDate())) {
            throw new RuntimeException("Deadline has passed, submission not allowed");
        }

        // 5. Validate student belongs to school
        AddStudent student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        if (!student.getSchool().getSchoolId().equals(dto.getSchoolId())) {
            throw new RuntimeException("Student does not belong to this school");
        }

        // 6. Check if already submitted (ignoring SEEN records)
        List<AssignmentSubmission> existing = submissionRepository.findByStudent_IdAndAssignment_Id(dto.getStudentId(), dto.getAssignmentId());
        AssignmentSubmission submission = null;
        
        if (!existing.isEmpty()) {
            AssignmentSubmission match = existing.get(0);
            if ("SEEN".equalsIgnoreCase(match.getStatus())) {
                submission = match;
            } else {
                throw new RuntimeException("Assignment already submitted by this student");
            }
        }

        if (submission == null) {
            submission = new AssignmentSubmission();
            submission.setAssignment(assignment);
            submission.setStudent(student);
            submission.setSchool(school);
        }
        
        submission.setFileUrl(dto.getFileUrl());
        submission.setSubmissionDate(LocalDateTime.now());
        submission.setStatus("SUBMITTED");
        submission.setRemarks("PENDING"); // default remark

        submissionRepository.save(submission);
    }

    // ==================== EDIT (Student updates before deadline) ====================
    @Override
    public void editSubmission(AssignmentSubmissionDTO dto) {
        // 1. Find existing submission
        AssignmentSubmission submission = submissionRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        // 2. Validate student owns this submission
        if (!submission.getStudent().getId().equals(dto.getStudentId())) {
            throw new RuntimeException("You can only edit your own submission");
        }

        // 3. Validate school
        if (!submission.getSchool().getSchoolId().equals(dto.getSchoolId())) {
            throw new RuntimeException("Submission does not belong to this school");
        }

        // 4. Check if already graded — cannot edit after grading
        if ("GRADED".equalsIgnoreCase(submission.getStatus())) {
            throw new RuntimeException("Cannot edit a graded submission");
        }

        // 5. Check deadline
        Assignment assignment = submission.getAssignment();
        if (assignment.getDueDate() != null && LocalDate.now().isAfter(assignment.getDueDate())) {
            throw new RuntimeException("Deadline has passed, editing not allowed");
        }

        // 6. Update file and reset remark
        submission.setFileUrl(dto.getFileUrl());
        submission.setSubmissionDate(LocalDateTime.now());
        submission.setStatus("SUBMITTED");
        submission.setRemarks("PENDING"); // reset to PENDING on edit

        submissionRepository.save(submission);
    }

    // ==================== GET BY ID ====================
    @Override
    public AssignmentSubmissionDTO getSubmissionById(Long id) {
        AssignmentSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        return toDTO(submission);
    }

    // ==================== GRADE (Teacher grades) ====================
    @Override
    public void gradeSubmission(AssignmentSubmissionDTO dto) {
        AssignmentSubmission submission = submissionRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        submission.setMarks(dto.getMarks());
        if (dto.getRemarks() != null) {
            submission.setRemarks(dto.getRemarks());
        }
        submission.setStatus("GRADED");

        submissionRepository.save(submission);
    }

    // ==================== HISTORY (Student sees own submissions) ====================
    @Override
    public List<AssignmentSubmissionDTO> getSubmissionsByStudent(Long studentId, Long schoolId) {
        return submissionRepository.findByStudent_IdAndSchool_SchoolId(studentId, schoolId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ==================== BY ASSIGNMENT (Teacher views — auto SEEN) ====================
    @Override
    public List<AssignmentSubmissionDTO> getSubmissionsByAssignment(Long assignmentId, Long schoolId) {
        List<AssignmentSubmission> submissions = submissionRepository
                .findByAssignment_IdAndSchool_SchoolId(assignmentId, schoolId);

        // Auto-set PENDING remarks to SEEN when teacher views
        for (AssignmentSubmission sub : submissions) {
            if ("PENDING".equalsIgnoreCase(sub.getRemarks())) {
                sub.setRemarks("SEEN");
                submissionRepository.save(sub);
            }
        }

        return submissions.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ==================== CHECK IF SUBMITTED ====================
    @Override
    public boolean hasStudentSubmitted(Long studentId, Long assignmentId) {
        return submissionRepository.existsByStudent_IdAndAssignment_Id(studentId, assignmentId);
    }

    // ==================== MARK AS SEEN ====================
    @Override
    public void markAsSeen(Long studentId, Long assignmentId, Long schoolId) {
        List<AssignmentSubmission> existing = submissionRepository.findByStudent_IdAndAssignment_Id(studentId, assignmentId);
        if (!existing.isEmpty()) {
            return;
        }

        School school = schoolRepository.findBySchoolId(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        AddStudent student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setSchool(school);
        submission.setStatus("SEEN");
        submission.setRemarks("UNSUBMITTED");

        submissionRepository.save(submission);
    }
}

