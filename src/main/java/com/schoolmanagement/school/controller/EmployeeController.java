package com.schoolmanagement.school.controller;

import com.schoolmanagement.school.dto.EmployeeDTO;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;

    public EmployeeController(EmployeeService employeeService,
                              SchoolRepository schoolRepository,
                              AcademicYearRepository academicYearRepository) {
        this.employeeService = employeeService;
        this.schoolRepository = schoolRepository;
        this.academicYearRepository = academicYearRepository;
    }

    private Map<String, Object> build(int code, String status, String message, Object data) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", code);
        r.put("status", status);
        r.put("message", message);
        r.put("data", data);
        return r;
    }

    // ── POST /employee/create ──────────────────────────────────────────────────
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody EmployeeDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getEmployeeName() == null || dto.getEmployeeName().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'employeeName' is required", null));
            if (dto.getEmailId() == null || dto.getEmailId().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'emailId' is required", null));
            if (dto.getMobileNumber() == null || dto.getMobileNumber().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'mobileNumber' is required", null));
            if (dto.getGender() != null && !dto.getGender().equals("MALE") && !dto.getGender().equals("FEMALE") && !dto.getGender().equals("OTHER"))
                return ResponseEntity.badRequest().body(build(400, "FAILED", "gender must be MALE, FEMALE, or OTHER", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (dto.getAcademicYearId() != null && !academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic year not found", null));

            employeeService.addEmployee(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Employee added successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(build(409, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ── POST /employee/update ──────────────────────────────────────────────────
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> update(@RequestBody EmployeeDTO dto) {
        try {
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            employeeService.updateEmployee(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Employee updated successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ── POST /employee/delete ──────────────────────────────────────────────────
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestBody EmployeeDTO dto) {
        try {
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            employeeService.deleteEmployee(dto.getId(), dto.getSchoolId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Employee deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ── POST /employee/getAll ──────────────────────────────────────────────────
    @PostMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getAll(@RequestBody EmployeeDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            List<EmployeeDTO> list = employeeService.getAll(dto.getSchoolId(), dto.getAcademicYearId());
            if (list.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No employees found", null));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("employees", list);
            return ResponseEntity.ok(build(200, "SUCCESS", "Employees fetched successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ── POST /employee/getActive ───────────────────────────────────────────────
    @PostMapping("/getActive")
    public ResponseEntity<Map<String, Object>> getActive(@RequestBody EmployeeDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            List<EmployeeDTO> list = employeeService.getActive(dto.getSchoolId(), dto.getAcademicYearId());
            if (list.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No active employees found", null));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("employees", list);
            return ResponseEntity.ok(build(200, "SUCCESS", "Active employees fetched successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ── POST /employee/getInactive ─────────────────────────────────────────────
    @PostMapping("/getInactive")
    public ResponseEntity<Map<String, Object>> getInactive(@RequestBody EmployeeDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            List<EmployeeDTO> list = employeeService.getInactive(dto.getSchoolId(), dto.getAcademicYearId());
            if (list.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No inactive employees found", null));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("employees", list);
            return ResponseEntity.ok(build(200, "SUCCESS", "Inactive employees fetched successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ── POST /employee/getById ─────────────────────────────────────────────────
    @PostMapping("/getById")
    public ResponseEntity<Map<String, Object>> getById(@RequestBody EmployeeDTO dto) {
        try {
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            EmployeeDTO result = employeeService.getById(dto.getId(), dto.getSchoolId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Employee fetched successfully", result));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ── POST /employee/countActive ─────────────────────────────────────────────
    @PostMapping("/countActive")
    public ResponseEntity<Map<String, Object>> countActive(@RequestBody EmployeeDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("count", employeeService.countActive(dto.getSchoolId()));
            return ResponseEntity.ok(build(200, "SUCCESS", "Active employee count fetched", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ── POST /employee/countInactive ───────────────────────────────────────────
    @PostMapping("/countInactive")
    public ResponseEntity<Map<String, Object>> countInactive(@RequestBody EmployeeDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("count", employeeService.countInactive(dto.getSchoolId()));
            return ResponseEntity.ok(build(200, "SUCCESS", "Inactive employee count fetched", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ── POST /employee/countTotal ──────────────────────────────────────────────
    @PostMapping("/countTotal")
    public ResponseEntity<Map<String, Object>> countTotal(@RequestBody EmployeeDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("count", employeeService.countTotal(dto.getSchoolId()));
            return ResponseEntity.ok(build(200, "SUCCESS", "Total employee count fetched", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ── POST /employee/checkEmail ──────────────────────────────────────────────
    @PostMapping("/checkEmail")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestBody EmployeeDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getEmailId() == null || dto.getEmailId().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'emailId' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("exists", employeeService.checkEmail(dto.getSchoolId(), dto.getEmailId()));
            return ResponseEntity.ok(build(200, "SUCCESS", "Email check successful", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ── POST /employee/checkMobile ─────────────────────────────────────────────
    @PostMapping("/checkMobile")
    public ResponseEntity<Map<String, Object>> checkMobile(@RequestBody EmployeeDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getMobileNumber() == null || dto.getMobileNumber().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'mobileNumber' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("exists", employeeService.checkMobile(dto.getSchoolId(), dto.getMobileNumber()));
            return ResponseEntity.ok(build(200, "SUCCESS", "Mobile check successful", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ── POST /employee/changeStatus ────────────────────────────────────────────
    @PostMapping("/changeStatus")
    public ResponseEntity<Map<String, Object>> changeStatus(@RequestBody EmployeeDTO dto) {
        try {
            if (dto.getId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'id' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getStatus() == null || dto.getStatus().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'status' is required", null));
            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            employeeService.changeStatus(dto.getId(), dto.getSchoolId(), dto.getStatus());
            return ResponseEntity.ok(build(200, "SUCCESS", "Employee status changed successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(build(409, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }
}