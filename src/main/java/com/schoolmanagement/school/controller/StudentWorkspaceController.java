package com.schoolmanagement.school.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.schoolmanagement.school.dto.AssignmentDTO;
import com.schoolmanagement.school.dto.AssignmentSubmissionDTO;
import com.schoolmanagement.school.dto.ResourceDTO;
import com.schoolmanagement.school.dto.StudyGroupDTO;
import com.schoolmanagement.school.dto.StudyGroupMemberDTO;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddDivisionRepository;
import com.schoolmanagement.school.repository.AddStandardRepository;
import com.schoolmanagement.school.repository.AddStudentRepository;
import com.schoolmanagement.school.repository.AddSubjectRepository;
import com.schoolmanagement.school.repository.AddTeacherRepository;
import com.schoolmanagement.school.repository.AssignmentRepository;
import com.schoolmanagement.school.repository.AssignmentSubmissionRepository;
import com.schoolmanagement.school.repository.ResourceRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.repository.StudyGroupRepository;
import com.schoolmanagement.school.service.AssignmentService;
import com.schoolmanagement.school.service.AssignmentSubmissionService;
import com.schoolmanagement.school.service.ResourceService;
import com.schoolmanagement.school.service.StudyGroupService;

@RestController
@RequestMapping("/workspace")
public class StudentWorkspaceController {

    private final AssignmentService assignmentService;
    private final AssignmentSubmissionService submissionService;
    private final StudyGroupService studyGroupService;
    private final ResourceService resourceService;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AddStandardRepository standardRepository;
    private final AddDivisionRepository divisionRepository;
    private final AddSubjectRepository subjectRepository;
    private final AddTeacherRepository teacherRepository;
    private final AddStudentRepository studentRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final ResourceRepository resourceRepository;
    private final StudyGroupRepository studyGroupRepository;

    public StudentWorkspaceController(AssignmentService assignmentService,
                                       AssignmentSubmissionService submissionService,
                                       StudyGroupService studyGroupService,
                                       ResourceService resourceService,
                                       SchoolRepository schoolRepository,
                                       AcademicYearRepository academicYearRepository,
                                       AddStandardRepository standardRepository,
                                       AddDivisionRepository divisionRepository,
                                       AddSubjectRepository subjectRepository,
                                       AddTeacherRepository teacherRepository,
                                       AddStudentRepository studentRepository,
                                       AssignmentRepository assignmentRepository,
                                       AssignmentSubmissionRepository submissionRepository,
                                       ResourceRepository resourceRepository,
                                       StudyGroupRepository studyGroupRepository) {
        this.assignmentService = assignmentService;
        this.submissionService = submissionService;
        this.studyGroupService = studyGroupService;
        this.resourceService = resourceService;
        this.schoolRepository = schoolRepository;
        this.academicYearRepository = academicYearRepository;
        this.standardRepository = standardRepository;
        this.divisionRepository = divisionRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.resourceRepository = resourceRepository;
        this.studyGroupRepository = studyGroupRepository;
    }

    // ── Helper ──────────────────────────────────────────────
    private Map<String, Object> build(int code, String status, String message, Object data) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", code); r.put("status", status);
        r.put("message", message); r.put("data", data);
        return r;
    }

    // ========================================================================================================
    //                                     ASSIGNMENT APIs (SCHOOL + TEACHER: create/update/delete/close)
    //                                                     (SCHOOL + TEACHER + STUDENT: get/list)
    // ========================================================================================================

    @PostMapping("/assignment/create")
    public ResponseEntity<Map<String, Object>> createAssignment(@RequestBody(required = false) AssignmentDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            if (dto.getTitle() == null || dto.getTitle().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'title' is required", null));
            if (dto.getSubjectId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'subjectId' is required", null));
            if (dto.getStandardId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'standardId' is required", null));
            if (dto.getDivisionId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'divisionId' is required", null));
            if (dto.getTeacherId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'teacherId' is required", null));
            if (dto.getDueDate() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'dueDate' is required", null));

            // FK existence checks
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));
            if (!standardRepository.existsById(dto.getStandardId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Standard not found", null));
            if (!divisionRepository.existsById(dto.getDivisionId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Division not found", null));
            if (!subjectRepository.existsById(dto.getSubjectId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Subject not found", null));
            if (!teacherRepository.existsById(dto.getTeacherId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Teacher not found", null));

            assignmentService.createAssignment(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Assignment created successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/assignment/get")
    public ResponseEntity<Map<String, Object>> getAssignment(@RequestBody(required = false) AssignmentDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));
            if (!assignmentRepository.existsById(dto.getId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Assignment not found", null));

            AssignmentDTO result = assignmentService.getAssignmentById(dto.getId(), dto.getSchoolId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Assignment retrieved successfully", result));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/assignment/update")
    public ResponseEntity<Map<String, Object>> updateAssignment(@RequestBody(required = false) AssignmentDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!assignmentRepository.existsById(dto.getId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Assignment not found", null));
            if (dto.getSubjectId() != null && !subjectRepository.existsById(dto.getSubjectId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Subject not found", null));
            if (dto.getStandardId() != null && !standardRepository.existsById(dto.getStandardId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Standard not found", null));
            if (dto.getDivisionId() != null && !divisionRepository.existsById(dto.getDivisionId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Division not found", null));

            assignmentService.updateAssignment(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Assignment updated successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/assignment/delete")
    public ResponseEntity<Map<String, Object>> deleteAssignment(@RequestBody(required = false) AssignmentDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!assignmentRepository.existsById(dto.getId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Assignment not found", null));

            assignmentService.deleteAssignment(dto.getId(), dto.getSchoolId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Assignment deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/assignment/getbyclassdivision")
    public ResponseEntity<Map<String, Object>> getAssignmentsByClassDivision(@RequestBody(required = false) AssignmentDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            if (dto.getStandardId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'standardId' is required", null));
            if (dto.getDivisionId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'divisionId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));
            if (!standardRepository.existsById(dto.getStandardId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Standard not found", null));
            if (!divisionRepository.existsById(dto.getDivisionId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Division not found", null));

            List<AssignmentDTO> assignments = assignmentService.getAssignmentsBySchoolAndStandardAndDivision(
                    dto.getSchoolId(), dto.getAcademicYearId(), dto.getStandardId(), dto.getDivisionId());

            if (assignments.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No assignments found for this class", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Assignments retrieved successfully", assignments));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/assignment/active")
    public ResponseEntity<Map<String, Object>> getActiveAssignments(@RequestBody(required = false) AssignmentDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));

            List<AssignmentDTO> assignments = assignmentService.getActiveAssignmentsBySchool(dto.getSchoolId(), dto.getAcademicYearId());

            if (assignments.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No active assignments found", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Active assignments retrieved successfully", assignments));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/assignment/close")
    public ResponseEntity<Map<String, Object>> closeAssignment(@RequestBody(required = false) AssignmentDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!assignmentRepository.existsById(dto.getId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Assignment not found", null));

            assignmentService.closeAssignment(dto.getId(), dto.getSchoolId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Assignment closed successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/assignment/getbyteacher")
    public ResponseEntity<Map<String, Object>> getAssignmentsByTeacher(@RequestBody(required = false) AssignmentDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getTeacherId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'teacherId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!teacherRepository.existsById(dto.getTeacherId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Teacher not found", null));

            List<AssignmentDTO> assignments = assignmentService.getAssignmentsByTeacher(dto.getSchoolId(), dto.getTeacherId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Assignments retrieved successfully", assignments));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    // ========================================================================================================
    //                                     SUBMISSION APIs (STUDENT: submit, edit, history)
    //                                                     (TEACHER: grade, byassignment → auto SEEN)
    // ========================================================================================================

    @PostMapping("/submission/submit")
    public ResponseEntity<Map<String, Object>> submitAssignment(@RequestBody(required = false) AssignmentSubmissionDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getAssignmentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'assignmentId' is required", null));
            if (dto.getStudentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getFileUrl() == null || dto.getFileUrl().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'fileUrl' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!assignmentRepository.existsById(dto.getAssignmentId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Assignment not found", null));
            if (!studentRepository.existsById(dto.getStudentId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Student not found", null));

            submissionService.submitAssignment(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Assignment submitted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(build(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/submission/seen")
    public ResponseEntity<Map<String, Object>> markAsSeen(@RequestBody(required = false) AssignmentSubmissionDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getAssignmentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'assignmentId' is required", null));
            if (dto.getStudentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!assignmentRepository.existsById(dto.getAssignmentId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Assignment not found", null));
            if (!studentRepository.existsById(dto.getStudentId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Student not found", null));

            submissionService.markAsSeen(dto.getStudentId(), dto.getAssignmentId(), dto.getSchoolId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Assignment marked as seen", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(build(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/submission/edit")
    public ResponseEntity<Map<String, Object>> editSubmission(@RequestBody(required = false) AssignmentSubmissionDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            if (dto.getStudentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getFileUrl() == null || dto.getFileUrl().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'fileUrl' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!submissionRepository.existsById(dto.getId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Submission not found", null));
            if (!studentRepository.existsById(dto.getStudentId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Student not found", null));

            submissionService.editSubmission(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Submission updated successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(build(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/submission/grade")
    public ResponseEntity<Map<String, Object>> gradeSubmission(@RequestBody(required = false) AssignmentSubmissionDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            if (dto.getMarks() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'marks' is required", null));

            if (!submissionRepository.existsById(dto.getId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Submission not found", null));

            submissionService.gradeSubmission(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Submission graded successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/submission/history")
    public ResponseEntity<Map<String, Object>> getSubmissionHistory(@RequestBody(required = false) AssignmentSubmissionDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getStudentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));
            if (!studentRepository.existsById(dto.getStudentId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Student not found", null));

            List<AssignmentSubmissionDTO> submissions = submissionService.getSubmissionsByStudent(dto.getStudentId(), dto.getSchoolId());

            if (submissions.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No submission history found for this student", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Submission history retrieved successfully", submissions));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/submission/byassignment")
    public ResponseEntity<Map<String, Object>> getSubmissionsByAssignment(@RequestBody(required = false) AssignmentSubmissionDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getAssignmentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'assignmentId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));
            if (!assignmentRepository.existsById(dto.getAssignmentId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Assignment not found", null));

            List<AssignmentSubmissionDTO> submissions = submissionService.getSubmissionsByAssignment(dto.getAssignmentId(), dto.getSchoolId());

            if (submissions.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No submissions found for this assignment", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Submissions retrieved successfully", submissions));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    // ========================================================================================================
    //                                     STUDY GROUP APIs (TEACHER: create/update/deactivate/addstudent/removestudent)
    //                                                      (ALL: get/active/bystandard/members)
    // ========================================================================================================

    @PostMapping("/studygroup/create")
    public ResponseEntity<Map<String, Object>> createStudyGroup(@RequestBody(required = false) StudyGroupDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            if (dto.getCreatedByTeacherId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'createdByTeacherId' is required", null));
            if (dto.getGroupName() == null || dto.getGroupName().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'groupName' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));
            if (!teacherRepository.existsById(dto.getCreatedByTeacherId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Teacher not found", null));
            if (dto.getStandardId() != null && !standardRepository.existsById(dto.getStandardId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Standard not found", null));
            if (dto.getDivisionId() != null && !divisionRepository.existsById(dto.getDivisionId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Division not found", null));

            studyGroupService.createStudyGroup(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Study group created successfully", dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(build(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/studygroup/get")
    public ResponseEntity<Map<String, Object>> getStudyGroup(@RequestBody(required = false) StudyGroupDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));

            if (!studyGroupRepository.existsById(dto.getId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Study Group not found", null));

            StudyGroupDTO result = studyGroupService.getStudyGroupById(dto.getId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Study group retrieved successfully", result));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/studygroup/update")
    public ResponseEntity<Map<String, Object>> updateStudyGroup(@RequestBody(required = false) StudyGroupDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));

            if (!studyGroupRepository.existsById(dto.getId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Study Group not found", null));

            studyGroupService.updateStudyGroup(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Study group updated successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/studygroup/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateStudyGroup(@RequestBody(required = false) StudyGroupDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));

            if (!studyGroupRepository.existsById(dto.getId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Study Group not found", null));

            studyGroupService.deactivateStudyGroup(dto.getId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Study group deactivated successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/studygroup/active")
    public ResponseEntity<Map<String, Object>> getActiveGroups(@RequestBody(required = false) StudyGroupDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));

            List<StudyGroupDTO> groups = studyGroupService.getActiveGroupsBySchool(dto.getSchoolId(), dto.getAcademicYearId());

            if (groups.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No active study groups found", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Active study groups retrieved successfully", groups));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/studygroup/bystandard")
    public ResponseEntity<Map<String, Object>> getGroupsByStandard(@RequestBody(required = false) StudyGroupDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            if (dto.getStandardId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'standardId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));
            if (!standardRepository.existsById(dto.getStandardId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Standard not found", null));

            List<StudyGroupDTO> groups = studyGroupService.getGroupsByStandard(dto.getSchoolId(), dto.getAcademicYearId(), dto.getStandardId());

            if (groups.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No study groups found for this standard", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Study groups retrieved successfully", groups));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/studygroup/mygroups")
    public ResponseEntity<Map<String, Object>> getMyStudyGroups(@RequestBody(required = false) StudyGroupMemberDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getStudentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));
            if (!studentRepository.existsById(dto.getStudentId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Student not found", null));

            List<StudyGroupDTO> groups = studyGroupService.getGroupsByStudent(dto.getStudentId(), dto.getAcademicYearId());

            if (groups.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No study groups found for this student", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Student's study groups retrieved successfully", groups));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/studygroup/addstudent")
    public ResponseEntity<Map<String, Object>> addStudentToGroup(@RequestBody(required = false) StudyGroupMemberDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            if (dto.getStudyGroupId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studyGroupId' is required", null));
            if (dto.getStudentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));
            if (!studyGroupRepository.existsById(dto.getStudyGroupId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Study Group not found", null));
            if (!studentRepository.existsById(dto.getStudentId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Student not found", null));

            studyGroupService.addStudentToGroup(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Student added to study group successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(build(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/studygroup/addstudents")
    public ResponseEntity<Map<String, Object>> addStudentsToGroup(@RequestBody(required = false) StudyGroupMemberDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            if (dto.getStudyGroupId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studyGroupId' is required", null));
            if (dto.getStudentIds() == null || dto.getStudentIds().isEmpty())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentIds' is required and cannot be empty", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));
            if (!studyGroupRepository.existsById(dto.getStudyGroupId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Study Group not found", null));

            studyGroupService.addStudentsToGroup(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Students added to study group successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(build(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/studygroup/removestudent")
    public ResponseEntity<Map<String, Object>> removeStudentFromGroup(@RequestBody(required = false) StudyGroupMemberDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getStudyGroupId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studyGroupId' is required", null));
            if (dto.getStudentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required", null));

            if (!studyGroupRepository.existsById(dto.getStudyGroupId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Study Group not found", null));
            if (!studentRepository.existsById(dto.getStudentId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Student not found", null));

            studyGroupService.removeStudentFromGroup(dto.getStudyGroupId(), dto.getStudentId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Student removed from study group successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/studygroup/members")
    public ResponseEntity<Map<String, Object>> getGroupMembers(@RequestBody(required = false) StudyGroupDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));

            if (!studyGroupRepository.existsById(dto.getId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Study Group not found", null));

            List<StudyGroupMemberDTO> members = studyGroupService.getGroupMembers(dto.getId());

            if (members.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No members found in this group", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Group members retrieved successfully", members));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    // ========================================================================================================
    //                                     RESOURCE APIs (SCHOOL + TEACHER: create/update/delete)
    //                                                   (SCHOOL + TEACHER + STUDENT: get/browse)
    // ========================================================================================================

    @PostMapping("/resource/create")
    public ResponseEntity<Map<String, Object>> createResource(@RequestBody(required = false) ResourceDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getTitle() == null || dto.getTitle().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'title' is required", null));
            if (dto.getResourceType() == null || dto.getResourceType().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'resourceType' is required", null));
            if (dto.getFileUrl() == null || dto.getFileUrl().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'fileUrl' is required", null));

            // Validate resourceType value
            String type = dto.getResourceType().toUpperCase();
            if (!type.equals("EBOOK") && !type.equals("VIDEO") && !type.equals("NOTE"))
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'resourceType' must be EBOOK, VIDEO, or NOTE", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (dto.getAcademicYearId() != null && !academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));
            if (dto.getStandardId() != null && !standardRepository.existsById(dto.getStandardId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Standard not found", null));
            if (dto.getSubjectId() != null && !subjectRepository.existsById(dto.getSubjectId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Subject not found", null));

            resourceService.createResource(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Resource created successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/resource/get")
    public ResponseEntity<Map<String, Object>> getResource(@RequestBody(required = false) ResourceDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));

            if (!resourceRepository.existsById(dto.getId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Resource not found", null));

            ResourceDTO result = resourceService.getResourceById(dto.getId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Resource retrieved successfully", result));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/resource/update")
    public ResponseEntity<Map<String, Object>> updateResource(@RequestBody(required = false) ResourceDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));

            if (!resourceRepository.existsById(dto.getId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Resource not found", null));

            if (dto.getResourceType() != null) {
                String type = dto.getResourceType().toUpperCase();
                if (!type.equals("EBOOK") && !type.equals("VIDEO") && !type.equals("NOTE"))
                    return ResponseEntity.badRequest().body(build(400, "FAILED", "'resourceType' must be EBOOK, VIDEO, or NOTE", null));
            }
            if (dto.getSubjectId() != null && !subjectRepository.existsById(dto.getSubjectId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Subject not found", null));
            if (dto.getStandardId() != null && !standardRepository.existsById(dto.getStandardId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Standard not found", null));

            resourceService.updateResource(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Resource updated successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/resource/delete")
    public ResponseEntity<Map<String, Object>> deleteResource(@RequestBody(required = false) ResourceDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));

            if (!resourceRepository.existsById(dto.getId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Resource not found", null));

            resourceService.deleteResource(dto.getId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Resource deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/resource/getall")
    public ResponseEntity<Map<String, Object>> getAllResources(@RequestBody(required = false) ResourceDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));

            List<ResourceDTO> resources = resourceService.getResourcesBySchool(dto.getSchoolId(), dto.getAcademicYearId());

            if (resources.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No resources found", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Resources retrieved successfully", resources));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/resource/bystandard")
    public ResponseEntity<Map<String, Object>> getResourcesByStandard(@RequestBody(required = false) ResourceDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            if (dto.getStandardId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'standardId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));
            if (!standardRepository.existsById(dto.getStandardId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Standard not found", null));

            List<ResourceDTO> resources = resourceService.getResourcesByStandard(dto.getSchoolId(), dto.getAcademicYearId(), dto.getStandardId());

            if (resources.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No resources found for this standard", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Resources retrieved successfully", resources));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/resource/bysubject")
    public ResponseEntity<Map<String, Object>> getResourcesBySubject(@RequestBody(required = false) ResourceDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            if (dto.getSubjectId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'subjectId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));
            if (!subjectRepository.existsById(dto.getSubjectId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Subject not found", null));

            List<ResourceDTO> resources = resourceService.getResourcesBySubject(dto.getSchoolId(), dto.getAcademicYearId(), dto.getSubjectId());

            if (resources.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No resources found for this subject", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Resources retrieved successfully", resources));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }

    @PostMapping("/resource/bytype")
    public ResponseEntity<Map<String, Object>> getResourcesByType(@RequestBody(required = false) ResourceDTO dto) {
        try {
            if (dto == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Request body is missing", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            if (dto.getResourceType() == null || dto.getResourceType().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'resourceType' is required", null));

            String type = dto.getResourceType().toUpperCase();
            if (!type.equals("EBOOK") && !type.equals("VIDEO") && !type.equals("NOTE"))
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'resourceType' must be EBOOK, VIDEO, or NOTE", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));

            List<ResourceDTO> resources = resourceService.getResourcesByType(dto.getSchoolId(), dto.getAcademicYearId(), type);

            if (resources.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No resources found for this type", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Resources retrieved successfully", resources));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error: " + e.getClass().getSimpleName() + " - " + e.getMessage(), null));
        }
    }
}
