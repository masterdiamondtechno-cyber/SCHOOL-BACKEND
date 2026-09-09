package com.schoolmanagement.school_admin_Controller;

import com.schoolmanagement.school_admin_DTO.BulkCreateResponseDTO;
import com.schoolmanagement.school_admin_DTO.DivisionTimetableResponseDTO;
import com.schoolmanagement.school_admin_DTO.SchoolTimetableResponseDTO;
import com.schoolmanagement.school_admin_DTO.TeacherTimetableResponseDTO;
import com.schoolmanagement.school_admin_DTO.TimetableDTO;
import com.schoolmanagement.school_admin_Repo.AcademicYearRepository;
import com.schoolmanagement.school_admin_Repo.AddDivisionRepository;
import com.schoolmanagement.school_admin_Repo.AddStandardRepository;
import com.schoolmanagement.school_admin_Repo.AddSubjectRepository;
import com.schoolmanagement.school_admin_Repo.AddTeacherRepository;
import com.schoolmanagement.school_admin_Repo.SchoolRepository;
import com.schoolmanagement.school_admin_Service.TimetableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/timetable")
public class TimetableController {

    private final TimetableService timetableService;
    private final SchoolRepository schoolRepository;
    private final AddStandardRepository standardRepository;
    private final AddDivisionRepository divisionRepository;
    private final AddSubjectRepository subjectRepository;
    private final AddTeacherRepository teacherRepository;
    private final AcademicYearRepository academicYearRepository;

    public TimetableController(TimetableService timetableService,
                               SchoolRepository schoolRepository,
                               AddStandardRepository standardRepository,
                               AddDivisionRepository divisionRepository,
                               AddSubjectRepository subjectRepository,
                               AddTeacherRepository teacherRepository,
                               AcademicYearRepository academicYearRepository) {
        this.timetableService = timetableService;
        this.schoolRepository = schoolRepository;
        this.standardRepository = standardRepository;
        this.divisionRepository = divisionRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.academicYearRepository = academicYearRepository;
    }

    // ==================== Helper ====================
    private Map<String, Object> build(int code, String status, String message, Object data) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", code);
        r.put("status", status);
        r.put("message", message);
        r.put("data", data);
        return r;
    }

    // ==================== CREATE ====================
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody TimetableDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "schoolId is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "School not found", null));
            if (dto.getStandardId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "standardId is required", null));
            if (!standardRepository.existsById(dto.getStandardId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "Standard not found", null));
            if (dto.getDivisionId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "divisionId is required", null));
            if (!divisionRepository.existsById(dto.getDivisionId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "Division not found", null));
            if (dto.getSubjectId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "subjectId is required", null));
            if (!subjectRepository.existsById(dto.getSubjectId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "Subject not found", null));
            if (dto.getDayOfWeek() == null || dto.getDayOfWeek().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "dayOfWeek is required", null));
            if (dto.getStartTime() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "startTime is required", null));
            if (dto.getEndTime() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "endTime is required", null));
            if (dto.getTeacherId() != null && !teacherRepository.existsById(dto.getTeacherId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "Teacher not found", null));

            timetableService.create(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Timetable entry created successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Something went wrong: " + e.getMessage(), null));
        }
    }

    // ==================== CREATE BULK ====================
    @PostMapping("/createBulk")
    public ResponseEntity<Map<String, Object>> createBulk(@RequestBody List<TimetableDTO> entries) {
        try {
            if (entries == null || entries.isEmpty())
                return ResponseEntity.badRequest()
                        .body(build(400, "FAILED", "Request body must be a non-empty array", null));

            BulkCreateResponseDTO result = timetableService.createBulk(entries);

            String message = result.getTotalFailed() == 0
                    ? "All entries created successfully"
                    : result.getTotalSaved() == 0
                    ? "All entries failed"
                    : result.getTotalSaved() + " entries saved, " + result.getTotalFailed() + " failed";

            return ResponseEntity.ok(build(200, "SUCCESS", message, result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Something went wrong: " + e.getMessage(), null));
        }
    }

    // ==================== GET BY DIVISION ====================
    @PostMapping("/division")
    public ResponseEntity<Map<String, Object>> getByDivision(@RequestBody TimetableDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "schoolId is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "academicYearId is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "Academic Year not found", null));
            if (dto.getStandardId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "standardId is required", null));
            if (!standardRepository.existsById(dto.getStandardId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "Standard not found", null));
            if (dto.getDivisionId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "divisionId is required", null));
            if (!divisionRepository.existsById(dto.getDivisionId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "Division not found", null));

            DivisionTimetableResponseDTO result = timetableService.getByDivisionStructured(
                    dto.getSchoolId(), dto.getStandardId(), dto.getDivisionId(), dto.getAcademicYearId());

            if (result.getTimetable().isEmpty())
                return ResponseEntity.status(404)
                        .body(build(404, "FAILED", "No timetable entries found for this division", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Timetable fetched successfully", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Something went wrong: " + e.getMessage(), null));
        }
    }

    // ==================== GET BY TEACHER ====================
    @PostMapping("/getByTeacher")
    public ResponseEntity<Map<String, Object>> getByTeacher(@RequestBody TimetableDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "schoolId is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "academicYearId is required", null));
            if (dto.getTeacherId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "teacherId is required", null));
            if (dto.getDate() == null || dto.getDate().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "date is required (yyyy-MM-dd)", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "Academic Year not found", null));
            if (!teacherRepository.existsById(dto.getTeacherId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "Teacher not found", null));

            TeacherTimetableResponseDTO dashboard = timetableService.getTeacherDashboard(
                    dto.getSchoolId(), dto.getTeacherId(), dto.getAcademicYearId(), dto.getDate());

            return ResponseEntity.ok(build(200, "SUCCESS", "Teacher timetable fetched successfully", dashboard));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(build(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Something went wrong: " + e.getMessage(), null));
        }
    }

    // ==================== GET BY CLASS ====================
    @PostMapping("/class")
    public ResponseEntity<Map<String, Object>> getByClass(@RequestBody TimetableDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "schoolId is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "academicYearId is required", null));
            if (dto.getTeacherId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "teacherId is required", null));
            if (dto.getStandardId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "standardId is required", null));
            if (dto.getDivisionId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "divisionId is required", null));
            if (dto.getDate() == null || dto.getDate().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "date is required (yyyy-MM-dd)", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "Academic Year not found", null));
            if (!teacherRepository.existsById(dto.getTeacherId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "Teacher not found", null));
            if (!standardRepository.existsById(dto.getStandardId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "Standard not found", null));
            if (!divisionRepository.existsById(dto.getDivisionId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "Division not found", null));

            Object result = timetableService.getByClass(
                    dto.getSchoolId(), dto.getTeacherId(), dto.getStandardId(), dto.getDivisionId(), dto.getAcademicYearId(), dto.getDate(), dto.getType());

            return ResponseEntity.ok(build(200, "SUCCESS", "Class timetable fetched successfully", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(build(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Something went wrong: " + e.getMessage(), null));
        }
    }

    // ==================== GET BY SCHOOL ====================
    @PostMapping("/school")
    public ResponseEntity<Map<String, Object>> getBySchool(@RequestBody TimetableDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "schoolId is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "academicYearId is required", null));
            if (dto.getDate() == null || dto.getDate().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "date is required (yyyy-MM-dd)", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "Academic Year not found", null));

            SchoolTimetableResponseDTO result = timetableService.getBySchool(
                    dto.getSchoolId(), dto.getAcademicYearId(), dto.getDate());

            if (result.getTimetable().isEmpty())
                return ResponseEntity.status(404)
                        .body(build(404, "FAILED", "No timetable entries found for this school on this date", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "School timetable fetched successfully", result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(build(400, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Something went wrong: " + e.getMessage(), null));
        }
    }

    // ==================== UPDATE ====================
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@RequestBody TimetableDTO dto) {
        try {
            if (dto.getTimetableId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "timetableId is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "schoolId is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "School not found", null));
            if (dto.getSubjectId() != null && !subjectRepository.existsById(dto.getSubjectId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "Subject not found", null));
            if (dto.getTeacherId() != null && !teacherRepository.existsById(dto.getTeacherId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "Teacher not found", null));

            timetableService.update(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Timetable entry updated successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Something went wrong: " + e.getMessage(), null));
        }
    }

    // ==================== DELETE ====================
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestBody TimetableDTO dto) {
        try {
            if (dto.getTimetableId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "timetableId is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "schoolId is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(404).body(build(404, "FAILED", "School not found", null));

            timetableService.delete(dto.getTimetableId(), dto.getSchoolId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Timetable entry deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(build(500, "FAILED", "Something went wrong: " + e.getMessage(), null));
        }
    }

    // ==================== TEACHER APP TIMETABLE ====================
    @PostMapping("/teacher-app")
    public ResponseEntity<com.schoolmanagement.school_admin_DTO.TeacherAppTimetableResponseDTO> getTeacherAppTimetable(
            @RequestBody com.schoolmanagement.school_admin_DTO.TeacherAppTimetableRequestDTO request) {

        if (request == null || request.getTeacherId() == null || request.getSchoolId() == null
                || request.getAcademicYearId() == null) {
            com.schoolmanagement.school_admin_DTO.TeacherAppTimetableResponseDTO errorResponse = new com.schoolmanagement.school_admin_DTO.TeacherAppTimetableResponseDTO();
            errorResponse.setCode(400);
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage("Missing required fields: teacherId, schoolId, or academicYearId");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        String targetType = (request.getType() != null && !request.getType().isBlank()) ? request.getType() : "day";

        try {
            com.schoolmanagement.school_admin_DTO.TeacherAppTimetableResponseDTO response = timetableService
                    .getTeacherAppTimetable(
                            targetType,
                            request.getDate(),
                            request.getTeacherId(),
                            request.getSchoolId(),
                            request.getAcademicYearId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            com.schoolmanagement.school_admin_DTO.TeacherAppTimetableResponseDTO errorResponse = new com.schoolmanagement.school_admin_DTO.TeacherAppTimetableResponseDTO();
            errorResponse.setCode(500);
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage(
                    "Internal Server Error: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getName()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== STUDENT TIMETABLE ====================
    @PostMapping("/getByStudent")
    public ResponseEntity<com.schoolmanagement.school_admin_DTO.StudentAppTimetableResponseDTO> getStudentTimetable(
            @RequestBody com.schoolmanagement.school_admin_DTO.StudentAppTimetableRequestDTO request) {

        if (request == null || request.getStudentId() == null || request.getSchoolId() == null
                || request.getAcademicYearId() == null) {
            com.schoolmanagement.school_admin_DTO.StudentAppTimetableResponseDTO errorResponse = new com.schoolmanagement.school_admin_DTO.StudentAppTimetableResponseDTO();
            errorResponse.setCode(400);
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage("Missing required fields: studentId, schoolId, or academicYearId");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        String targetType = (request.getType() != null && !request.getType().isBlank()) ? request.getType() : "day";

        try {
            com.schoolmanagement.school_admin_DTO.StudentAppTimetableResponseDTO response = timetableService
                    .getStudentTimetable(
                            targetType,
                            request.getDate(),
                            request.getStudentId(),
                            request.getSchoolId(),
                            request.getAcademicYearId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            com.schoolmanagement.school_admin_DTO.StudentAppTimetableResponseDTO errorResponse = new com.schoolmanagement.school_admin_DTO.StudentAppTimetableResponseDTO();
            errorResponse.setCode(400);
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            e.printStackTrace();
            com.schoolmanagement.school_admin_DTO.StudentAppTimetableResponseDTO errorResponse = new com.schoolmanagement.school_admin_DTO.StudentAppTimetableResponseDTO();
            errorResponse.setCode(500);
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage(
                    "Internal Server Error: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getName()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}