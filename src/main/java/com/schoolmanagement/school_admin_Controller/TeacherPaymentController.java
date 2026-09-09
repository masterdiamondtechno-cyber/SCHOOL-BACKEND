package com.schoolmanagement.school_admin_Controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.schoolmanagement.school_admin_DTO.TeacherPaymentDTO;
import com.schoolmanagement.school_admin_DTO.TeacherPaymentPageDataDTO;
import com.schoolmanagement.school_admin_Entity.TeacherPayment;
import com.schoolmanagement.school_admin_Repo.AcademicYearRepository;
import com.schoolmanagement.school_admin_Repo.AddTeacherRepository;
import com.schoolmanagement.school_admin_Repo.SchoolRepository;
import com.schoolmanagement.school_admin_Repo.TeacherPaymentRepository;
import com.schoolmanagement.school_admin_Service.TeacherPaymentService;

@RestController
@RequestMapping("/teacher-payment")
public class TeacherPaymentController {

    private final TeacherPaymentService teacherPaymentService;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AddTeacherRepository teacherRepository;
    private final TeacherPaymentRepository teacherPaymentRepository;

    public TeacherPaymentController(TeacherPaymentService teacherPaymentService,
                                     SchoolRepository schoolRepository,
                                     AcademicYearRepository academicYearRepository,
                                     AddTeacherRepository teacherRepository,
                                     TeacherPaymentRepository teacherPaymentRepository) {
        this.teacherPaymentService = teacherPaymentService;
        this.schoolRepository = schoolRepository;
        this.academicYearRepository = academicYearRepository;
        this.teacherRepository = teacherRepository;
        this.teacherPaymentRepository = teacherPaymentRepository;
    }

    // ── Helper ──────────────────────────────────────────────
    private Map<String, Object> build(int code, String status, String message, Object data) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", code); r.put("status", status);
        r.put("message", message); r.put("data", data);
        return r;
    }

    private static final List<String> VALID_MONTHS = Arrays.asList(
            "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
            "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER");

    private static final List<String> VALID_STATUSES = Arrays.asList("PAID", "PENDING", "ON_HOLD");

    // ===================================================
    // ============= PAGE DATA (Primary Endpoint) ========
    // ===================================================

    /**
     * POST /teacher-payment/getPageData
     * Feeds entire UI page in one call — summary cards + payments list.
     */
    @PostMapping("/getPageData")
    public ResponseEntity<Map<String, Object>> getPageData(@RequestBody TeacherPaymentDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));

            TeacherPaymentPageDataDTO pageData = teacherPaymentService.getPageData(
                    dto.getSchoolId(), dto.getAcademicYearId(),
                    dto.getStatusFilter(), dto.getPaymentMonth(), dto.getPaymentYear());

            return ResponseEntity.ok(build(200, "SUCCESS", "Teacher payment data fetched successfully", pageData));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ===================================================
    // ============= CREATE ==============================
    // ===================================================

    /**
     * POST /teacher-payment/create
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody TeacherPaymentDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            if (dto.getTeacherId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'teacherId' is required", null));
            if (dto.getPaymentMonth() == null || dto.getPaymentMonth().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'paymentMonth' is required", null));
            if (dto.getPaymentYear() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'paymentYear' is required", null));
            if (dto.getBasicSalary() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'basicSalary' is required", null));
            if (dto.getStatus() == null || dto.getStatus().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'status' is required", null));

            if (!VALID_MONTHS.contains(dto.getPaymentMonth().toUpperCase()))
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Invalid paymentMonth. Must be one of: " + VALID_MONTHS, null));

            if (!VALID_STATUSES.contains(dto.getStatus().toUpperCase()))
                return ResponseEntity.badRequest().body(build(400, "FAILED", "Invalid status. Must be one of: " + VALID_STATUSES, null));

            if ("PAID".equalsIgnoreCase(dto.getStatus())) {
                if (dto.getPaymentMode() == null || dto.getPaymentMode().isBlank())
                    return ResponseEntity.badRequest().body(build(400, "FAILED", "'paymentMode' is required when status is PAID", null));
                if (dto.getPaymentDate() == null)
                    return ResponseEntity.badRequest().body(build(400, "FAILED", "'paymentDate' is required when status is PAID", null));
            }

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));

            teacherPaymentService.createPayment(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Teacher payment created successfully", null));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(build(409, "FAILED", e.getMessage(), null));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ===================================================
    // ============= UPDATE ==============================
    // ===================================================

    /**
     * POST /teacher-payment/update
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updatePayment(@RequestBody TeacherPaymentDTO dto) {
        try {
            if (dto.getTeacherPaymentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'teacherPaymentId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            teacherPaymentService.updatePayment(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Teacher payment updated successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ===================================================
    // ============= DELETE ==============================
    // ===================================================

    /**
     * POST /teacher-payment/delete
     */
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> deletePayment(@RequestBody TeacherPaymentDTO dto) {
        try {
            if (dto.getTeacherPaymentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'teacherPaymentId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            teacherPaymentService.deletePayment(dto.getTeacherPaymentId(), dto.getSchoolId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Teacher payment deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ===================================================
    // ============= MARK AS PAID ========================
    // ===================================================

    /**
     * POST /teacher-payment/markAsPaid
     * For the "Pay" button in the UI (PENDING → PAID action).
     */
    @PostMapping("/markAsPaid")
    public ResponseEntity<Map<String, Object>> markAsPaid(@RequestBody TeacherPaymentDTO dto) {
        try {
            if (dto.getTeacherPaymentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'teacherPaymentId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getPaymentMode() == null || dto.getPaymentMode().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'paymentMode' is required", null));
            if (dto.getPaymentDate() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'paymentDate' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            teacherPaymentService.markAsPaid(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Payment marked as paid successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ===================================================
    // ============= GET BY TEACHER ======================
    // ===================================================

    /**
     * POST /teacher-payment/getByTeacher
     */
    @PostMapping("/getByTeacher")
    public ResponseEntity<Map<String, Object>> getByTeacher(@RequestBody TeacherPaymentDTO dto) {
        try {
            if (dto.getTeacherId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'teacherId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            List<TeacherPayment> payments = teacherPaymentRepository.findByTeacher_IdAndSchool_SchoolId(
                    dto.getTeacherId(), dto.getSchoolId());

            List<Map<String, Object>> paymentList = new ArrayList<>();
            for (TeacherPayment p : payments) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("teacherPaymentId", p.getTeacherPaymentId());
                row.put("receiptNumber", p.getReceiptNumber());
                row.put("paymentMonth", p.getPaymentMonth());
                row.put("paymentYear", p.getPaymentYear());
                row.put("basicSalary", p.getBasicSalary());
                row.put("bonus", p.getBonus());
                row.put("deductions", p.getDeductions());
                row.put("netAmountPaid", p.getNetAmountPaid());
                row.put("status", p.getStatus());
                row.put("paymentMode", p.getPaymentMode());
                row.put("paymentDate", p.getPaymentDate());
                row.put("transactionId", p.getTransactionId());
                row.put("remark", p.getRemark());
                paymentList.add(row);
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("payments", paymentList);

            return ResponseEntity.ok(build(200, "SUCCESS", "Teacher payments fetched successfully", data));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }
}
