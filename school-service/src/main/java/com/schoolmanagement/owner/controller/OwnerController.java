package com.schoolmanagement.owner.controller;

import com.schoolmanagement.owner.dto.*;
import com.schoolmanagement.owner.service.OwnerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Owner (Franchise Manager) REST Controller.
 *
 * Base path  : /owner/...
 * Auth       : ROLE_OWNER (enforced in SecurityConfig)
 * Guarantee  : Every endpoint calls OwnerService which internally validates
 *              that the ownerId exists before executing any query.
 *
 * NOTE: APIs are now POST to receive parameters in request body.
 */
@RestController
@RequestMapping("/owner")
public class OwnerController {

    @Autowired
    private OwnerService ownerService;

    // ─────────────────────────────────────────────────────────────
    //  1. DASHBOARD
    //  POST /owner/dashboard
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/dashboard")
    public ResponseEntity<OwnerDashboardDTO> getDashboard(@RequestBody OwnerDashboardRequestDTO req) {
        OwnerDashboardDTO dto = ownerService.getDashboard(req.getOwnerId(), req.getYear(), req.getMonth());
        return ResponseEntity.ok(dto);
    }

    // ─────────────────────────────────────────────────────────────
    //  2. SCHOOLS
    //  POST /owner/schools
    //  POST /owner/schools/stats
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/schools")
    public ResponseEntity<PagedResponseDTO<OwnerSchoolDTO>> getSchools(@RequestBody OwnerSchoolsRequestDTO req) {
        Pageable pageable = PageRequest.of(req.getPage(), req.getSize());
        PagedResponseDTO<OwnerSchoolDTO> result =
                ownerService.getSchools(req.getOwnerId(), req.getStatus(), req.getCity(), req.getSearch(), pageable);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/schools/stats")
    public ResponseEntity<OwnerSchoolStatsDTO> getSchoolStats(@RequestBody OwnerSchoolStatsRequestDTO req) {
        OwnerSchoolStatsDTO dto = ownerService.getSchoolStats(req.getOwnerId());
        return ResponseEntity.ok(dto);
    }

    @PostMapping(value = "/schools/request", consumes = {"multipart/form-data"})
    public ResponseEntity<java.util.Map<String, Object>> requestSchoolBranch(
            @ModelAttribute com.schoolmanagement.school.dto.SchoolDTO request,
            @RequestParam(value = "schoolLogo", required = false) org.springframework.web.multipart.MultipartFile logoFile) {
        
        java.util.Map<String, Object> result = ownerService.requestSchoolBranch(request.getOwnerId(), request, logoFile);
        return ResponseEntity.ok(result);
    }

    // ─────────────────────────────────────────────────────────────
    //  3. TEACHERS
    //  POST /owner/teachers
    //  POST /owner/teachers/stats
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/teachers")
    public ResponseEntity<PagedResponseDTO<OwnerTeacherDTO>> getTeachers(@RequestBody OwnerTeachersRequestDTO req) {
        Pageable pageable = PageRequest.of(req.getPage(), req.getSize());
        PagedResponseDTO<OwnerTeacherDTO> result =
                ownerService.getTeachers(req.getOwnerId(), req.getSchoolId(), req.getStatus(), req.getSearch(), pageable);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/teachers/stats")
    public ResponseEntity<java.util.Map<String, Object>> getTeacherStats(@RequestBody OwnerDashboardRequestDTO req) {
        return ResponseEntity.ok(ownerService.getTeacherStats(req.getOwnerId()));
    }

    // ─────────────────────────────────────────────────────────────
    //  4. STUDENTS
    //  POST /owner/students
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/students")
    public ResponseEntity<PagedResponseDTO<OwnerStudentDTO>> getStudents(@RequestBody OwnerStudentsRequestDTO req) {
        Pageable pageable = PageRequest.of(req.getPage(), req.getSize());
        PagedResponseDTO<OwnerStudentDTO> result =
                ownerService.getStudents(req.getOwnerId(), req.getSchoolId(), req.getClassId(), req.getFeeStatus(), req.getGender(), req.getSearch(), pageable);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/students/stats")
    public ResponseEntity<java.util.Map<String, Object>> getStudentStats(@RequestBody OwnerDashboardRequestDTO req) {
        return ResponseEntity.ok(ownerService.getStudentStats(req.getOwnerId()));
    }

    // ─────────────────────────────────────────────────────────────
    //  4b. PROFILE & SETTINGS
    //  POST /owner/profile
    //  POST /owner/profile/update
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/profile")
    public ResponseEntity<java.util.Map<String, Object>> getProfile(@RequestBody OwnerDashboardRequestDTO req) {
        return ResponseEntity.ok(ownerService.getOwnerProfile(req.getOwnerId()));
    }

    @PostMapping("/profile/update")
    public ResponseEntity<java.util.Map<String, Object>> updateProfile(@RequestBody java.util.Map<String, Object> req) {
        Long ownerId = ((Number) req.get("ownerId")).longValue();
        return ResponseEntity.ok(ownerService.updateOwnerProfile(ownerId, req));
    }

    // ─────────────────────────────────────────────────────────────
    //  5. FINANCE
    //  POST /owner/finance/summary
    //  POST /owner/finance/branch-wise
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/finance/summary")
    public ResponseEntity<OwnerFinanceSummaryDTO> getFinanceSummary(@RequestBody OwnerFinanceRequestDTO req) {
        OwnerFinanceSummaryDTO dto =
                ownerService.getFinanceSummary(req.getOwnerId(), req.getStartDate(), req.getEndDate(), req.getSchoolId());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/finance/branch-wise")
    public ResponseEntity<List<OwnerFinanceBranchDTO>> getBranchWiseFinance(@RequestBody OwnerFinanceRequestDTO req) {
        List<OwnerFinanceBranchDTO> result =
                ownerService.getBranchWiseFinance(req.getOwnerId(), req.getStartDate(), req.getEndDate(), req.getSchoolId());
        return ResponseEntity.ok(result);
    }

    // ─────────────────────────────────────────────────────────────
    //  6. REPORTS
    //  POST /owner/reports/summary
    //  POST /owner/reports/revenue
    //  POST /owner/reports/attendance
    //  POST /owner/reports/enrollments
    //  POST /owner/reports/fees
    // ─────────────────────────────────────────────────────────────

    @PostMapping("/reports/summary")
    public ResponseEntity<OwnerReportDTO> getReportSummary(@RequestBody OwnerReportRequestDTO req) {
        return ResponseEntity.ok(
                ownerService.getReportSummary(req.getOwnerId(), req.getStartDate(), req.getEndDate(), req.getSchoolId(), req.getClassId(), req.getGroupBy() != null ? req.getGroupBy() : "month"));
    }

    @PostMapping("/reports/revenue")
    public ResponseEntity<OwnerReportDTO> getRevenueReport(@RequestBody OwnerReportRequestDTO req) {
        return ResponseEntity.ok(
                ownerService.getRevenueReport(req.getOwnerId(), req.getStartDate(), req.getEndDate(), req.getSchoolId(), req.getGroupBy() != null ? req.getGroupBy() : "month"));
    }

    @PostMapping("/reports/attendance")
    public ResponseEntity<OwnerReportDTO> getAttendanceReport(@RequestBody OwnerReportRequestDTO req) {
        return ResponseEntity.ok(
                ownerService.getAttendanceReport(req.getOwnerId(), req.getStartDate(), req.getEndDate(), req.getSchoolId(), req.getClassId(), req.getGroupBy() != null ? req.getGroupBy() : "month"));
    }

    @PostMapping("/reports/enrollments")
    public ResponseEntity<OwnerReportDTO> getEnrollmentReport(@RequestBody OwnerReportRequestDTO req) {
        return ResponseEntity.ok(
                ownerService.getEnrollmentReport(req.getOwnerId(), req.getStartDate(), req.getEndDate(), req.getSchoolId(), req.getGroupBy() != null ? req.getGroupBy() : "month"));
    }

    @PostMapping("/reports/fees")
    public ResponseEntity<OwnerReportDTO> getFeeReport(@RequestBody OwnerReportRequestDTO req) {
        return ResponseEntity.ok(
                ownerService.getFeeReport(req.getOwnerId(), req.getStartDate(), req.getEndDate(), req.getSchoolId(), req.getClassId(), req.getGroupBy() != null ? req.getGroupBy() : "month"));
    }
}
