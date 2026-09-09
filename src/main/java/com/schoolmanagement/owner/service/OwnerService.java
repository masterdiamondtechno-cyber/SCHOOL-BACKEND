package com.schoolmanagement.owner.service;

import com.schoolmanagement.owner.dto.*;
import com.schoolmanagement.owner.entity.Owner;
import com.schoolmanagement.owner.repository.OwnerRepository;
import com.schoolmanagement.owner.repository.OwnerSchoolQueryRepository;
import com.schoolmanagement.app_admin_DTO.OwnerApprovalRequestDTO;
import com.schoolmanagement.app_admin_Service.EmailService;
import com.schoolmanagement.Config.PasswordGenerator;
import com.schoolmanagement.school_admin_DTO.SchoolDTO;
import com.schoolmanagement.school_admin_Entity.School;
import com.schoolmanagement.school_admin_Service.SchoolService;
import com.schoolmanagement.school_admin_Repo.SchoolRepository;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

/**
 * Business logic for the Owner (Franchise Manager) Panel.
 *
 * SECURITY CONTRACT:
 *   - Every public method validates that ownerId exists before proceeding.
 *   - All data queries pass ownerId so only that owner's schools are returned.
 *   - No cross-owner data leakage is possible.
 */
@Service
public class OwnerService {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private com.schoolmanagement.school_admin_Repo.RoleRepository roleRepository;

    @Autowired
    private OwnerSchoolQueryRepository queryRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SchoolService schoolService;

    @Autowired
    private SchoolRepository schoolRepository;

    // ─────────────────────────────────────────────────────────────
    //  SECURITY GUARD
    // ─────────────────────────────────────────────────────────────

    private Owner validateOwner(Long ownerId) {
        return ownerRepository.findById(ownerId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Owner not found with id: " + ownerId));
    }

    // ─────────────────────────────────────────────────────────────
    //  DASHBOARD
    // ─────────────────────────────────────────────────────────────

    public OwnerDashboardDTO getDashboard(Long ownerId, Integer year, Integer month) {
        validateOwner(ownerId);

        int filterYear  = (year  != null) ? year  : LocalDate.now().getYear();

        // Core counts
        long   branches      = queryRepo.countSchoolsByOwnerId(ownerId);
        long   students      = queryRepo.countStudentsByOwnerId(ownerId);

        // Revenue: current period vs previous period
        double currentRevenue;
        double prevRevenue;

        if (month != null) {
            currentRevenue = queryRepo.sumRevenueByOwnerIdAndYearMonth(ownerId, filterYear, month);
            int prevMonth  = month == 1 ? 12 : month - 1;
            int prevYear   = month == 1 ? filterYear - 1 : filterYear;
            prevRevenue    = queryRepo.sumRevenueByOwnerIdAndYearMonth(ownerId, prevYear, prevMonth);
        } else {
            currentRevenue = queryRepo.sumRevenueByOwnerIdAndYear(ownerId, filterYear);
            prevRevenue    = queryRepo.sumRevenueByOwnerIdAndYear(ownerId, filterYear - 1);
        }

        // Safe growth rate (avoid divide-by-zero)
        double growthRate = (prevRevenue == 0)
            ? (currentRevenue > 0 ? 100.0 : 0.0)
            : Math.round(((currentRevenue - prevRevenue) / prevRevenue) * 10000.0) / 100.0;

        // Per-school performance matrix
        List<Map<String, Object>> matrix = queryRepo.getPerformanceMatrix(ownerId, filterYear);

        // Recent alerts: flag schools with attendance < 70%
        List<Map<String, Object>> alerts = new ArrayList<>();
        for (Map<String, Object> school : matrix) {
            Object pct = school.get("attendancePct");
            double attendancePct = pct != null ? ((Number) pct).doubleValue() : 0.0;
            if (attendancePct < 70.0 && attendancePct > 0) {
                Map<String, Object> alert = new LinkedHashMap<>();
                alert.put("type",       "LOW_ATTENDANCE");
                alert.put("schoolId",   school.get("schoolId"));
                alert.put("schoolName", school.get("schoolName"));
                alert.put("message",    String.format("Attendance at %.1f%% — below 70%% threshold", attendancePct));
                alerts.add(alert);
            }
        }

        OwnerDashboardDTO dto = new OwnerDashboardDTO();
        dto.setTotalBranches(branches);
        dto.setTotalStudents(students);
        dto.setTotalRevenue(currentRevenue);
        dto.setGrowthRate(growthRate);
        dto.setFilterYear(filterYear);
        dto.setFilterMonth(month);
        dto.setPerformanceMatrix(matrix);
        dto.setRecentAlerts(alerts);
        return dto;
    }

    // ─────────────────────────────────────────────────────────────
    //  SCHOOLS
    // ─────────────────────────────────────────────────────────────

    public PagedResponseDTO<OwnerSchoolDTO> getSchools(Long ownerId,
                                                        String status,
                                                        String city,
                                                        String search,
                                                        Pageable pageable) {
        validateOwner(ownerId);
        List<OwnerSchoolDTO> data  = queryRepo.getSchoolsByOwner(ownerId, status, city, search, pageable);
        long total                 = queryRepo.countSchoolsByOwnerWithFilters(ownerId, status, city, search);
        return new PagedResponseDTO<>(data, pageable.getPageNumber(), pageable.getPageSize(), total);
    }

    public OwnerSchoolStatsDTO getSchoolStats(Long ownerId) {
        validateOwner(ownerId);

        long totalSchools    = queryRepo.countSchoolsByOwnerId(ownerId);
        long totalStudents   = queryRepo.countStudentsByOwnerId(ownerId);
        double totalRevenue  = queryRepo.sumRevenueByOwnerIdAndYear(ownerId, LocalDate.now().getYear());

        // Active/Inactive via filtered count
        long activeSchools   = queryRepo.countSchoolsByOwnerWithFilters(ownerId, "ACTIVE", null, null);
        long inactiveSchools = totalSchools - activeSchools;

        long totalTeachers   = queryRepo.countTeachersByOwnerWithFilters(ownerId, null, null, null);

        double avgStudents = totalSchools == 0 ? 0.0
            : Math.round((double) totalStudents / totalSchools * 100.0) / 100.0;
        double avgRevenue  = totalSchools == 0 ? 0.0
            : Math.round(totalRevenue / totalSchools * 100.0) / 100.0;

        OwnerSchoolStatsDTO dto = new OwnerSchoolStatsDTO();
        dto.setTotalSchools(totalSchools);
        dto.setActiveSchools(activeSchools);
        dto.setInactiveSchools(inactiveSchools);
        dto.setTotalStudents(totalStudents);
        dto.setTotalTeachers(totalTeachers);
        dto.setTotalRevenue(totalRevenue);
        dto.setAvgStudentsPerSchool(avgStudents);
        dto.setAvgRevenuePerSchool(avgRevenue);
        return dto;
    }

    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> requestSchoolBranch(Long ownerId, SchoolDTO dto, MultipartFile logoFile) {
        validateOwner(ownerId);

        // Explicit duplicate checks
        if (schoolRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }
        if (schoolRepository.existsByPhoneNo(dto.getPhoneNo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number already exists");
        }
        if (schoolRepository.existsByUdiseNo(dto.getUdiseNo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UDISE NO already exists");
        }

        // Set owner on DTO so the createSchool service handles mapping
        dto.setOwnerId(ownerId);
        dto.setSchoolStatus("INACTIVE");
        dto.setSubscriptionStatus("PENDING");

        // Delegate school creation to the standard SchoolService
        School savedSchool = schoolService.createSchool(dto, logoFile);

        Map<String, Object> res = new HashMap<>();
        res.put("status", "SUCCESS");
        res.put("message", "School branch requested successfully");
        res.put("schoolId", savedSchool.getSchoolId());
        return res;
    }

    // ─────────────────────────────────────────────────────────────
    //  TEACHERS
    // ─────────────────────────────────────────────────────────────

    public PagedResponseDTO<OwnerTeacherDTO> getTeachers(Long ownerId,
                                                          Long schoolId,
                                                          String status,
                                                          String search,
                                                          Pageable pageable) {
        validateOwner(ownerId);
        List<OwnerTeacherDTO> data = queryRepo.getTeachersByOwner(ownerId, schoolId, status, search, pageable);
        long total                 = queryRepo.countTeachersByOwnerWithFilters(ownerId, schoolId, status, search);
        return new PagedResponseDTO<>(data, pageable.getPageNumber(), pageable.getPageSize(), total);
    }

    public Map<String, Object> getTeacherStats(Long ownerId) {
        validateOwner(ownerId);
        return queryRepo.getTeacherStats(ownerId);
    }

    // ─────────────────────────────────────────────────────────────
    //  STUDENTS
    // ─────────────────────────────────────────────────────────────

    public PagedResponseDTO<OwnerStudentDTO> getStudents(Long ownerId,
                                                          Long schoolId,
                                                          Long classId,
                                                          String feeStatus,
                                                          String gender,
                                                          String search,
                                                          Pageable pageable) {
        validateOwner(ownerId);
        List<OwnerStudentDTO> data = queryRepo.getStudentsByOwner(ownerId, schoolId, classId, feeStatus, gender, search, pageable);
        long total                 = queryRepo.countStudentsByOwnerWithFilters(ownerId, schoolId, classId, gender, search);
        return new PagedResponseDTO<>(data, pageable.getPageNumber(), pageable.getPageSize(), total);
    }

    public Map<String, Object> getStudentStats(Long ownerId) {
        validateOwner(ownerId);
        return queryRepo.getStudentStats(ownerId);
    }

    // ─────────────────────────────────────────────────────────────
    //  PROFILE & SETTINGS
    // ─────────────────────────────────────────────────────────────

    public Map<String, Object> getOwnerProfile(Long ownerId) {
        Owner owner = validateOwner(ownerId);
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("ownerId", owner.getOwnerId());
        profile.put("name", owner.getName());
        profile.put("fullName", owner.getFullName());
        profile.put("email", owner.getEmail());
        profile.put("phoneNo", owner.getPhoneNo());
        profile.put("address", owner.getAddress());
        profile.put("dob", owner.getDob());
        profile.put("gender", owner.getGender());
        profile.put("aadharCard", owner.getAadharCard());
        profile.put("panCard", owner.getPanCard());
        profile.put("fullNameAsPerAadhar", owner.getFullNameAsPerAadhar());
        profile.put("registrationType", owner.getRegistrationType());
        return profile;
    }

    public Map<String, Object> updateOwnerProfile(Long ownerId, Map<String, Object> req) {
        Owner owner = validateOwner(ownerId);

        if (req.containsKey("name")) owner.setName((String) req.get("name"));
        if (req.containsKey("fullName")) owner.setFullName((String) req.get("fullName"));
        if (req.containsKey("email")) owner.setEmail((String) req.get("email"));
        if (req.containsKey("phoneNo")) owner.setPhoneNo((String) req.get("phoneNo"));
        if (req.containsKey("address")) owner.setAddress((String) req.get("address"));
        if (req.containsKey("dob")) owner.setDob((String) req.get("dob"));
        if (req.containsKey("gender")) owner.setGender((String) req.get("gender"));
        if (req.containsKey("aadharCard")) owner.setAadharCard((String) req.get("aadharCard"));
        if (req.containsKey("panCard")) owner.setPanCard((String) req.get("panCard"));
        if (req.containsKey("fullNameAsPerAadhar")) owner.setFullNameAsPerAadhar((String) req.get("fullNameAsPerAadhar"));
        if (req.containsKey("registrationType")) owner.setRegistrationType((String) req.get("registrationType"));

        ownerRepository.save(owner);

        Map<String, Object> res = new HashMap<>();
        res.put("status", "SUCCESS");
        res.put("message", "Profile updated successfully");
        res.put("profile", getOwnerProfile(ownerId));
        return res;
    }

    // ─────────────────────────────────────────────────────────────
    //  FINANCE
    // ─────────────────────────────────────────────────────────────

    public OwnerFinanceSummaryDTO getFinanceSummary(Long ownerId,
                                                     LocalDate startDate,
                                                     LocalDate endDate,
                                                     Long schoolId) {
        validateOwner(ownerId);
        Map<String, Object> raw = queryRepo.getFinanceSummary(ownerId, startDate, endDate, schoolId);

        OwnerFinanceSummaryDTO dto = new OwnerFinanceSummaryDTO();
        dto.setTotalBilled(raw.get("totalBilled")         != null ? ((Number) raw.get("totalBilled")).doubleValue()       : 0.0);
        dto.setTotalCollected(raw.get("totalCollected")   != null ? ((Number) raw.get("totalCollected")).doubleValue()    : 0.0);
        dto.setPendingAmount(raw.get("pendingAmount")     != null ? ((Number) raw.get("pendingAmount")).doubleValue()     : 0.0);
        dto.setTransactionCount(raw.get("transactionCount") != null ? ((Number) raw.get("transactionCount")).longValue() : 0L);
        dto.setStartDate(startDate != null ? startDate.toString() : null);
        dto.setEndDate(endDate     != null ? endDate.toString()   : null);
        return dto;
    }

    public List<OwnerFinanceBranchDTO> getBranchWiseFinance(Long ownerId,
                                                              LocalDate startDate,
                                                              LocalDate endDate,
                                                              Long schoolId) {
        validateOwner(ownerId);
        return queryRepo.getBranchWiseFinance(ownerId, startDate, endDate, schoolId);
    }

    // ─────────────────────────────────────────────────────────────
    //  REPORTS
    // ─────────────────────────────────────────────────────────────

    public OwnerReportDTO getReportSummary(Long ownerId,
                                            LocalDate startDate,
                                            LocalDate endDate,
                                            Long schoolId,
                                            Long classId,
                                            String groupBy) {
        validateOwner(ownerId);
        requireDateRange(startDate, endDate);

        // Combine revenue + enrollment into summary
        List<Map<String, Object>> revenue    = queryRepo.getRevenueReport(ownerId, startDate, endDate, schoolId, groupBy);
        List<Map<String, Object>> enrollment = queryRepo.getEnrollmentReport(ownerId, startDate, endDate, schoolId, groupBy);

        // Merge into a single summary list (keyed by period)
        Map<Object, Map<String, Object>> merged = new LinkedHashMap<>();
        for (Map<String, Object> r : revenue) {
            Object period = r.get("period");
            merged.computeIfAbsent(period, k -> { Map<String, Object> m = new LinkedHashMap<>(); m.put("period", period); return m; })
                  .put("revenue", r.get("revenue"));
        }
        for (Map<String, Object> e : enrollment) {
            Object period = e.get("period");
            merged.computeIfAbsent(period, k -> { Map<String, Object> m = new LinkedHashMap<>(); m.put("period", period); return m; })
                  .put("enrollments", e.get("enrollments"));
        }

        return buildReport("summary", startDate, endDate, schoolId, classId, groupBy,
                           new ArrayList<>(merged.values()));
    }

    public OwnerReportDTO getRevenueReport(Long ownerId,
                                            LocalDate startDate,
                                            LocalDate endDate,
                                            Long schoolId,
                                            String groupBy) {
        validateOwner(ownerId);
        requireDateRange(startDate, endDate);
        List<Map<String, Object>> data = queryRepo.getRevenueReport(ownerId, startDate, endDate, schoolId, groupBy);
        return buildReport("revenue", startDate, endDate, schoolId, null, groupBy, data);
    }

    public OwnerReportDTO getAttendanceReport(Long ownerId,
                                               LocalDate startDate,
                                               LocalDate endDate,
                                               Long schoolId,
                                               Long classId,
                                               String groupBy) {
        validateOwner(ownerId);
        requireDateRange(startDate, endDate);
        List<Map<String, Object>> data = queryRepo.getAttendanceReport(ownerId, startDate, endDate, schoolId, classId, groupBy);
        return buildReport("attendance", startDate, endDate, schoolId, classId, groupBy, data);
    }

    public OwnerReportDTO getEnrollmentReport(Long ownerId,
                                               LocalDate startDate,
                                               LocalDate endDate,
                                               Long schoolId,
                                               String groupBy) {
        validateOwner(ownerId);
        requireDateRange(startDate, endDate);
        List<Map<String, Object>> data = queryRepo.getEnrollmentReport(ownerId, startDate, endDate, schoolId, groupBy);
        return buildReport("enrollments", startDate, endDate, schoolId, null, groupBy, data);
    }

    public OwnerReportDTO getFeeReport(Long ownerId,
                                        LocalDate startDate,
                                        LocalDate endDate,
                                        Long schoolId,
                                        Long classId,
                                        String groupBy) {
        validateOwner(ownerId);
        requireDateRange(startDate, endDate);
        List<Map<String, Object>> data = queryRepo.getFeeReport(ownerId, startDate, endDate, schoolId, classId, groupBy);
        return buildReport("fees", startDate, endDate, schoolId, classId, groupBy, data);
    }

    // ─────────────────────────────────────────────────────────────
    //  OWNER CRUD (registration + lookup)
    // ─────────────────────────────────────────────────────────────

    public Owner findByEmailOrPhone(String username) {
        return ownerRepository.findByEmailOrPhoneNo(username, username).orElse(null);
    }

    public Optional<Owner> findById(Long ownerId) {
        return ownerRepository.findById(ownerId);
    }

    public Owner save(Owner owner) {
        return ownerRepository.save(owner);
    }

    // ─────────────────────────────────────────────────────────────
    //  PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────

    private void requireDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "startDate and endDate are mandatory for report endpoints");
        }
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "startDate must not be after endDate");
        }
    }

    private OwnerReportDTO buildReport(String type,
                                        LocalDate startDate, LocalDate endDate,
                                        Long schoolId, Long classId,
                                        String groupBy,
                                        List<Map<String, Object>> data) {
        OwnerReportDTO dto = new OwnerReportDTO();
        dto.setReportType(type);
        dto.setStartDate(startDate != null ? startDate.toString() : null);
        dto.setEndDate(endDate     != null ? endDate.toString()   : null);
        dto.setGroupBy(groupBy != null ? groupBy : "month");
        dto.setSchoolId(schoolId);
        dto.setClassId(classId);
        dto.setData(data != null ? data : Collections.emptyList());
        return dto;
    }

    // ─────────────────────────────────────────────────────────────
    //  APP ADMIN
    // ─────────────────────────────────────────────────────────────
    public PagedResponseDTO<com.schoolmanagement.app_admin_DTO.OwnerListDTO> getAllOwners(
            String status, String search, Pageable pageable) {
        org.springframework.data.domain.Page<com.schoolmanagement.app_admin_DTO.OwnerListDTO> page =
                ownerRepository.findAllOwnersWithSchoolCount(status, search, pageable);

        return new PagedResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    // ─────────────────────────────────────────────────────────────
    //  OWNER APPROVAL / REJECTION (App Admin)
    // ─────────────────────────────────────────────────────────────

    public Map<String, Object> approveOrRejectOwner(OwnerApprovalRequestDTO request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Find owner
            if (request.getOwnerId() == null) {
                response.put("status", "FAILED");
                response.put("message", "ownerId is required");
                return response;
            }

            Optional<Owner> ownerOpt = ownerRepository.findById(request.getOwnerId());
            if (ownerOpt.isEmpty()) {
                response.put("status", "FAILED");
                response.put("message", "Owner not found with ID: " + request.getOwnerId());
                return response;
            }

            Owner owner = ownerOpt.get();
            String status = request.getApprovalRejectionStatus();

            if ("APPROVED".equalsIgnoreCase(status)) {
                // 2a. Generate password
                String rawPassword = PasswordGenerator.generatePassword(10);
                String encodedPassword = passwordEncoder.encode(rawPassword);

                // 2b. Update owner
                owner.setPassword(encodedPassword);
                owner.setStatus("ACTIVE");
                ownerRepository.save(owner);

                // 2c. Send approval email with credentials
                emailService.sendOwnerApprovalEmail(owner.getEmail(), rawPassword);

                response.put("status", "SUCCESS");
                response.put("message", "Owner approved successfully. Login credentials sent to " + owner.getEmail());

            } else if ("REJECTED".equalsIgnoreCase(status)) {
                // 3a. Update owner
                owner.setStatus("REJECTED");
                ownerRepository.save(owner);

                // 3b. Send rejection email
                emailService.sendOwnerRejectionEmail(owner.getEmail(), request.getRejectedReason());

                response.put("status", "SUCCESS");
                response.put("message", "Owner rejected successfully. Rejection email sent to " + owner.getEmail());

            } else {
                response.put("status", "FAILED");
                response.put("message", "Invalid status. Expected APPROVED or REJECTED.");
            }

        } catch (Exception e) {
            response.put("status", "FAILED");
            response.put("message", "An error occurred: " + e.getMessage());
        }

        return response;
    }

    public Map<String, Object> createOwner(Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String name = request.get("name");
            String email = request.get("email");
            String phoneNo = request.get("phoneNo");
            if (name == null || email == null || phoneNo == null) {
                response.put("status", "FAILED");
                response.put("message", "Name, email, and phoneNo are required");
                return response;
            }

            if (ownerRepository.findByEmailOrPhoneNo(email, phoneNo).isPresent()) {
                response.put("status", "FAILED");
                response.put("message", "Owner with this email or phone number already exists");
                return response;
            }

            Owner owner = new Owner();
            owner.setName(name);
            owner.setFullName(name);
            owner.setEmail(email);
            owner.setPhoneNo(phoneNo);
            owner.setUsername(email);
            
            // Generate a random temporary password
            String rawPassword = PasswordGenerator.generatePassword(10);
            owner.setPassword(passwordEncoder.encode(rawPassword));
            owner.setStatus("ACTIVE");
            owner.setRegistrationType("SINGLE");

            // Set role to OWNER (Id 6)
            roleRepository.findById(6L).ifPresent(owner::setRole);

            ownerRepository.save(owner);

            // Send password to owner's email
            emailService.sendAdminPasswordEmail(email, rawPassword);

            response.put("status", "SUCCESS");
            response.put("message", "Owner created successfully. Login credentials sent to " + email);
            
            Map<String, Object> data = new HashMap<>();
            data.put("ownerId", owner.getOwnerId());
            data.put("name", owner.getName());
            data.put("email", owner.getEmail());
            data.put("phoneNo", owner.getPhoneNo());
            response.put("data", data);

        } catch (Exception e) {
            response.put("status", "FAILED");
            response.put("message", "An error occurred: " + e.getMessage());
        }
        return response;
    }
}
