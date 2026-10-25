package com.schoolmanagement.school.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.schoolmanagement.school.dto.ExamDTO;
import com.schoolmanagement.school.dto.ExamMarksDTO;
import com.schoolmanagement.school.dto.HallTicketDTO;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddDivisionRepository;
import com.schoolmanagement.school.repository.AddStandardRepository;
import com.schoolmanagement.school.repository.AddStudentRepository;
import com.schoolmanagement.school.repository.AddSubjectRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.service.ExamService;

@RestController
@RequestMapping("/exam")
public class ExamController {

    private final ExamService examService;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AddStandardRepository standardRepository;
    private final AddDivisionRepository divisionRepository;
    private final AddStudentRepository studentRepository;
    private final AddSubjectRepository subjectRepository;

    public ExamController(ExamService examService,
                          SchoolRepository schoolRepository,
                          AcademicYearRepository academicYearRepository,
                          AddStandardRepository standardRepository,
                          AddDivisionRepository divisionRepository,
                          AddStudentRepository studentRepository,
                          AddSubjectRepository subjectRepository) {
        this.examService = examService;
        this.schoolRepository = schoolRepository;
        this.academicYearRepository = academicYearRepository;
        this.standardRepository = standardRepository;
        this.divisionRepository = divisionRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
    }

    // ── Helper ──────────────────────────────────────────────
    private Map<String, Object> build(int code, String status, String message, Object data) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", code); r.put("status", status);
        r.put("message", message); r.put("data", data);
        return r;
    }

    // ===== EXAM CRUD =====

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody ExamDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            if (dto.getStandardId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'standardId' is required", null));
            if (dto.getDivisionId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'divisionId' is required", null));
            if (dto.getSubjectId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'subjectId' is required", null));
            if (dto.getExamName() == null || dto.getExamName().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'examName' is required", null));

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

            examService.createExam(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Exam created successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    @PostMapping("/getBySchool")
    public ResponseEntity<Map<String, Object>> getBySchool(@RequestBody ExamDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));

            List<ExamDTO> list = examService.getAllBySchoolId(dto.getSchoolId(), dto.getAcademicYearId());
            if (list.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No exams found for this school", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Exams fetched successfully", list));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    @PostMapping("/getByDivision")
    public ResponseEntity<Map<String, Object>> getByDivision(@RequestBody ExamDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getStandardId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'standardId' is required", null));
            if (dto.getDivisionId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'divisionId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));
            if (!standardRepository.existsById(dto.getStandardId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Standard not found", null));
            if (!divisionRepository.existsById(dto.getDivisionId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Division not found", null));

            List<ExamDTO> list = examService.getByDivision(dto.getSchoolId(), dto.getStandardId(), dto.getDivisionId(), dto.getAcademicYearId());
            if (list.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No exams found for this division", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Exams fetched successfully", list));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@RequestBody ExamDTO dto) {
        try {
            if (dto.getExamId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'examId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() != null && !academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));
            if (dto.getSubjectId() != null && !subjectRepository.existsById(dto.getSubjectId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Subject not found", null));

            examService.updateExam(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Exam updated successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestBody ExamDTO dto) {
        try {
            if (dto.getExamId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'examId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            examService.deleteExam(dto.getExamId(), dto.getSchoolId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Exam and associated marks deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ===== MARKS =====

    @PostMapping("/marks/saveBulk")
    public ResponseEntity<Map<String, Object>> saveMarksBulk(@RequestBody ExamMarksDTO dto) {
        try {
            if (dto.getExamId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'examId' is required", null));
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

            examService.saveMarksBulk(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Marks saved successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    @PostMapping("/marks/getByExam")
    public ResponseEntity<Map<String, Object>> getMarksByExam(@RequestBody ExamMarksDTO dto) {
        try {
            if (dto.getExamId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'examId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));

            List<ExamMarksDTO> list = examService.getMarksByExam(dto.getExamId(), dto.getSchoolId(), dto.getAcademicYearId());
            if (list.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No marks found for this exam", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Marks fetched successfully", list));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    @PostMapping("/marks/getByStudent")
    public ResponseEntity<Map<String, Object>> getMarksByStudent(@RequestBody ExamMarksDTO dto) {
        try {
            if (dto.getExamId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'examId' is required", null));
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

            List<ExamMarksDTO> list = examService.getMarksByStudent(dto.getExamId(), dto.getStudentId(), dto.getSchoolId(), dto.getAcademicYearId());
            if (list.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No marks found for this student", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Marks fetched successfully", list));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ===== STUDENT-FACING APIs =====

    @PostMapping("/getByStudent")
    public ResponseEntity<Map<String, Object>> getByStudent(@RequestBody ExamDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            if (dto.getStudentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));
            if (!studentRepository.existsById(dto.getStudentId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Student not found", null));

            List<ExamDTO> list = examService.getExamsByStudent(dto.getSchoolId(), dto.getAcademicYearId(), dto.getStudentId());
            if (list.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No exams found for this student", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Exams fetched successfully", list));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    @PostMapping("/hallTicket")
    public ResponseEntity<Map<String, Object>> getHallTicket(@RequestBody ExamDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            if (dto.getStudentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required", null));
            if (dto.getExamId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'examId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));
            if (!studentRepository.existsById(dto.getStudentId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Student not found", null));

            HallTicketDTO hallTicket = examService.getHallTicket(dto.getSchoolId(), dto.getAcademicYearId(), dto.getStudentId(), dto.getExamId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Hall ticket fetched successfully", hallTicket));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }
}
