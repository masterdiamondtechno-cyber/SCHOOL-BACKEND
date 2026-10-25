package com.schoolmanagement.school.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.schoolmanagement.school.dto.FeeCollectionPageDataDTO;
import com.schoolmanagement.school.dto.FeePaymentDTO;
import com.schoolmanagement.school.dto.FeeStructureDTO;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddStandardRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.service.FeeService;

@RestController
@RequestMapping("/fee")
public class FeeController {

    private final FeeService feeService;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AddStandardRepository standardRepository;

    public FeeController(FeeService feeService,
                         SchoolRepository schoolRepository,
                         AcademicYearRepository academicYearRepository,
                         AddStandardRepository standardRepository) {
        this.feeService = feeService;
        this.schoolRepository = schoolRepository;
        this.academicYearRepository = academicYearRepository;
        this.standardRepository = standardRepository;
    }

    // ── Helper ──────────────────────────────────────────────
    private Map<String, Object> build(int code, String status, String message, Object data) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", code); r.put("status", status);
        r.put("message", message); r.put("data", data);
        return r;
    }

    // ===================================================
    // =================== FEE STRUCTURE =================
    // ===================================================

    /**
     * POST /fee/structure/create
     * Create a new fee structure for a standard.
     */
    @PostMapping("/structure/create")
    public ResponseEntity<Map<String, Object>> createStructure(@RequestBody FeeStructureDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            if (dto.getStandardId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'standardId' is required", null));
            if (dto.getFeeName() == null || dto.getFeeName().isBlank())
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'feeName' is required", null));
            if (dto.getAmount() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'amount' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));
            if (!standardRepository.existsById(dto.getStandardId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Standard not found", null));

            feeService.createStructure(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Fee structure created successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    /**
     * POST /fee/structure/getByStandard
     * Get all fee structures defined for a specific standard + academic year.
     */
    @PostMapping("/structure/getByStandard")
    public ResponseEntity<Map<String, Object>> getByStandard(@RequestBody FeeStructureDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getStandardId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'standardId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));
            if (!standardRepository.existsById(dto.getStandardId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Standard not found", null));

            List<FeeStructureDTO> list = feeService.getStructuresByStandard(dto.getSchoolId(), dto.getStandardId(), dto.getAcademicYearId());
            if (list.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No fee structures found", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Fee structures fetched successfully", list));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    /**
     * POST /fee/structure/update
     * Update an existing fee structure by feeStructureId.
     */
    @PostMapping("/structure/update")
    public ResponseEntity<Map<String, Object>> updateStructure(@RequestBody FeeStructureDTO dto) {
        try {
            if (dto.getFeeStructureId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'feeStructureId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (dto.getAcademicYearId() != null && !academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));

            feeService.updateStructure(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Fee structure updated successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    /**
     * POST /fee/structure/delete
     * Delete a fee structure by feeStructureId + schoolId.
     */
    @PostMapping("/structure/delete")
    public ResponseEntity<Map<String, Object>> deleteStructure(@RequestBody FeeStructureDTO dto) {
        try {
            if (dto.getFeeStructureId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'feeStructureId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            feeService.deleteStructure(dto.getFeeStructureId(), dto.getSchoolId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Fee structure deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ===================================================
    // ============= FEE PAYMENT — PAGE LOAD ============
    // ===================================================

    /**
     * POST /fee/payment/getPageData
     *
     * ★ SINGLE consolidated endpoint for the Fee Collection UI page.
     *
     * Returns in one call:
     *  - summary  → { totalCollected, totalPending, paidCount, partialCount, unpaidCount }
     *  - records  → enriched list of payments with studentName, className, rollNo,
     *               feeName, feeType, totalAmount, amountDue, dueDate, paymentDate,
     *               paymentMode, receiptNumber, status, remark
     *
     * Request body: { "schoolId": 1, "academicYearId": 2 }
     */
    @PostMapping("/payment/getPageData")
    public ResponseEntity<Map<String, Object>> getPageData(@RequestBody FeePaymentDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));

            FeeCollectionPageDataDTO pageData = feeService.getPageData(dto.getSchoolId(), dto.getAcademicYearId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Fee collection data fetched successfully", pageData));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    // ===================================================
    // ============= FEE PAYMENT — CRUD ==================
    // ===================================================

    /**
     * POST /fee/payment/create
     * Record a new fee payment (Collect Fee modal submit).
     *
     * Required: schoolId, studentId, academicYearId, amountPaid, paymentDate
     */
    @PostMapping("/payment/create")
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody FeePaymentDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getStudentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));
            if (dto.getAmountPaid() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'amountPaid' is required", null));
            if (dto.getPaymentDate() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'paymentDate' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));

            feeService.createPayment(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Payment recorded successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    /**
     * POST /fee/payment/getByStudent
     * Get all payment records for a specific student.
     *
     * Required: schoolId, studentId, academicYearId
     */
    @PostMapping("/payment/getByStudent")
    public ResponseEntity<Map<String, Object>> getPaymentsByStudent(@RequestBody FeePaymentDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getStudentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'studentId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));

            List<FeePaymentDTO> list = feeService.getPaymentsByStudent(dto.getSchoolId(), dto.getStudentId(), dto.getAcademicYearId());
            if (list.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No payment records found for this student", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Payments fetched successfully", list));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    /**
     * POST /fee/payment/update
     * Update an existing payment record (Edit button action).
     *
     * Required: paymentId, schoolId
     * Optional: amountPaid, paymentDate, paymentMode, transactionId, remark, status, feeStructureId
     */
    @PostMapping("/payment/update")
    public ResponseEntity<Map<String, Object>> updatePayment(@RequestBody FeePaymentDTO dto) {
        try {
            if (dto.getPaymentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'paymentId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            feeService.updatePayment(dto);
            return ResponseEntity.ok(build(200, "SUCCESS", "Payment updated successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    /**
     * POST /fee/payment/delete
     * Delete a payment record (Delete button action).
     *
     * Required: paymentId, schoolId
     */
    @PostMapping("/payment/delete")
    public ResponseEntity<Map<String, Object>> deletePayment(@RequestBody FeePaymentDTO dto) {
        try {
            if (dto.getPaymentId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'paymentId' is required", null));
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));

            feeService.deletePayment(dto.getPaymentId(), dto.getSchoolId());
            return ResponseEntity.ok(build(200, "SUCCESS", "Payment deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }

    /**
     * POST /fee/payment/getAll
     * Get all payments for a school + academic year (for internal/admin use).
     * For the UI page, prefer /fee/payment/getPageData instead.
     *
     * Required: schoolId, academicYearId
     */
    @PostMapping("/payment/getAll")
    public ResponseEntity<Map<String, Object>> getAllPayments(@RequestBody FeePaymentDTO dto) {
        try {
            if (dto.getSchoolId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'schoolId' is required", null));
            if (dto.getAcademicYearId() == null)
                return ResponseEntity.badRequest().body(build(400, "FAILED", "'academicYearId' is required", null));

            if (!schoolRepository.existsById(dto.getSchoolId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "School not found", null));
            if (!academicYearRepository.existsById(dto.getAcademicYearId()))
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "Academic Year not found", null));

            List<FeePaymentDTO> list = feeService.getAllPayments(dto.getSchoolId(), dto.getAcademicYearId());
            if (list.isEmpty())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", "No payment records found", null));

            return ResponseEntity.ok(build(200, "SUCCESS", "Payments fetched successfully", list));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "FAILED", e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(build(500, "FAILED", "Internal server error", null));
        }
    }
}
