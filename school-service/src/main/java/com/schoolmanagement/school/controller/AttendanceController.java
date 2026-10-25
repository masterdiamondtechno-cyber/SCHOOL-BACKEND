package com.schoolmanagement.school.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.schoolmanagement.school.dto.AttendanceDTO;
import com.schoolmanagement.school.service.AttendanceService;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/markBulk")
    public ResponseEntity<Map<String, Object>> markBulk(@RequestBody AttendanceDTO dto) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            if (dto.getSchoolId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "schoolId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getStandardId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "standardId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getDivisionId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "divisionId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getAcademicYearId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "academicYearId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getSubjectId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "subjectId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getAcademicYearId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "academicYearId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getEntries() == null || dto.getEntries().isEmpty()) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "entries list is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            attendanceService.markBulk(dto);
            response.put("code", 200); response.put("status", "SUCCESS");
            response.put("message", "Attendance marked successfully"); response.put("data", null);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("code", 404); response.put("status", "FAILED");
            response.put("message", e.getMessage()); response.put("data", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("code", 500); response.put("status", "FAILED");
            response.put("message", "Internal server error"); response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Get attendance for a specific subject on a date (per-lecture)
    @PostMapping("/getByDateAndSubject")
    public ResponseEntity<Map<String, Object>> getByDateAndSubject(@RequestBody AttendanceDTO dto) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            if (dto.getSchoolId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "schoolId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getStandardId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "standardId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getDivisionId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "divisionId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getAttendanceDate() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "attendanceDate is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getSubjectId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "subjectId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getAcademicYearId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "academicYearId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            List<AttendanceDTO> list = attendanceService.getByDateAndSubject(
                    dto.getSchoolId(), dto.getStandardId(), dto.getDivisionId(), dto.getAttendanceDate(), dto.getSubjectId(), dto.getAcademicYearId());
            if (list.isEmpty()) {
                response.put("code", 404); response.put("status", "FAILED");
                response.put("message", "No attendance records found for this subject"); response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response.put("code", 200); response.put("status", "SUCCESS");
            response.put("message", "Attendance fetched successfully"); response.put("data", list);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500); response.put("status", "FAILED");
            response.put("message", "Internal server error"); response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Get all attendance for a date (all subjects)
    @PostMapping("/getByDate")
    public ResponseEntity<Map<String, Object>> getByDate(@RequestBody AttendanceDTO dto) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            if (dto.getSchoolId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "schoolId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getStandardId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "standardId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getDivisionId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "divisionId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getAttendanceDate() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "attendanceDate is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getAcademicYearId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "academicYearId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            List<AttendanceDTO> list = attendanceService.getByDate(
                    dto.getSchoolId(), dto.getStandardId(), dto.getDivisionId(), dto.getAttendanceDate(), dto.getAcademicYearId());
            if (list.isEmpty()) {
                response.put("code", 404); response.put("status", "FAILED");
                response.put("message", "No attendance records found for this date"); response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response.put("code", 200); response.put("status", "SUCCESS");
            response.put("message", "Attendance fetched successfully"); response.put("data", list);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500); response.put("status", "FAILED");
            response.put("message", "Internal server error"); response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/getByStudent")
    public ResponseEntity<Map<String, Object>> getByStudent(@RequestBody AttendanceDTO dto) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            if (dto.getSchoolId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "schoolId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getStudentId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "studentId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getFromDate() == null || dto.getToDate() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "fromDate and toDate are required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getAcademicYearId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "academicYearId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            List<AttendanceDTO> list = attendanceService.getByStudent(
                    dto.getSchoolId(), dto.getStudentId(), dto.getFromDate(), dto.getToDate(), dto.getAcademicYearId());
            if (list.isEmpty()) {
                response.put("code", 404); response.put("status", "FAILED");
                response.put("message", "No attendance records found for this student"); response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            response.put("code", 200); response.put("status", "SUCCESS");
            response.put("message", "Attendance fetched successfully"); response.put("data", list);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500); response.put("status", "FAILED");
            response.put("message", "Internal server error"); response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@RequestBody AttendanceDTO dto) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            if (dto.getAttendanceId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "attendanceId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            attendanceService.update(dto);
            response.put("code", 200); response.put("status", "SUCCESS");
            response.put("message", "Attendance updated successfully"); response.put("data", null);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("code", 404); response.put("status", "FAILED");
            response.put("message", e.getMessage()); response.put("data", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            response.put("code", 500); response.put("status", "FAILED");
            response.put("message", "Internal server error"); response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/summary/today")
    public ResponseEntity<Map<String, Object>> todaySummary(@RequestBody AttendanceDTO dto) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            if (dto.getSchoolId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "schoolId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (dto.getAcademicYearId() == null) {
                response.put("code", 400); response.put("status", "FAILED");
                response.put("message", "academicYearId is required"); response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }
            long present = attendanceService.countTodayPresent(dto.getSchoolId(), dto.getAcademicYearId());
            long total = attendanceService.countTodayTotal(dto.getSchoolId(), dto.getAcademicYearId());
            Map<String, Object> summaryData = new LinkedHashMap<>();
            summaryData.put("present", present);
            summaryData.put("total", total);
            response.put("code", 200); response.put("status", "SUCCESS");
            response.put("message", "Today's attendance summary fetched successfully");
            response.put("data", summaryData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500); response.put("status", "FAILED");
            response.put("message", "Internal server error"); response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
