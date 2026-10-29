package com.schoolmanagement.school.service.impl;

import com.schoolmanagement.school.dto.TeacherLeaveDTO;
import com.schoolmanagement.school.entity.AcademicYear;
import com.schoolmanagement.school.entity.AddTeacher;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.entity.TeacherLeave;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddTeacherRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.repository.TeacherLeaveRepository;
import com.schoolmanagement.school.service.EmailService;
import com.schoolmanagement.school.service.TeacherLeaveService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeacherLeaveServiceImpl implements TeacherLeaveService {

    private final TeacherLeaveRepository leaveRepository;
    private final AddTeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;
    private final EmailService emailService;

    public TeacherLeaveServiceImpl(TeacherLeaveRepository leaveRepository,
                                   AddTeacherRepository teacherRepository,
                                   SchoolRepository schoolRepository,
                                   AcademicYearRepository academicYearRepository,
                                   EmailService emailService) {
        this.leaveRepository      = leaveRepository;
        this.teacherRepository    = teacherRepository;
        this.schoolRepository     = schoolRepository;
        this.academicYearRepository = academicYearRepository;
        this.emailService         = emailService;
    }

    // ── JWT helper: get logged-in teacher from SecurityContext ─────────────────
    // Pattern copied from TicketServiceImpl.getCurrentUsername()
    private AddTeacher getLoggedInTeacher() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new RuntimeException("Unauthorized");
        String username = auth.getName();
        return teacherRepository.findByEmailIdOrMobileNumber(username, username)
                .orElseThrow(() -> new RuntimeException("Authenticated teacher not found"));
    }

    // ── Auto-generate leave ref ID: LV-YYYY-001 ───────────────────────────────
    // Pattern copied from TicketServiceImpl.generateTicketId()
    private String generateLeaveRefId(Long schoolId) {
        int year = LocalDate.now().getYear();
        String prefix = "LV-" + year + "-";
        String max = leaveRepository.findMaxLeaveRefId();
        int next = 1;
        if (max != null && max.startsWith(prefix)) {
            try {
                next = Integer.parseInt(max.replace(prefix, "")) + 1;
            } catch (Exception ignored) {}
        }
        return String.format("%s%03d", prefix, next);
    }

    // ── Entity → response map ─────────────────────────────────────────────────
    private Map<String, Object> toMap(TeacherLeave l) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",                   l.getId());
        m.put("leaveRefId",           l.getLeaveRefId());
        m.put("teacherId",            l.getTeacher().getId());
        m.put("teacherName",          l.getTeacher().getTeacherName());
        m.put("leaveType",            l.getLeaveType());
        m.put("fromDate",             l.getFromDate());
        m.put("toDate",               l.getToDate());
        m.put("totalDays",            l.getTotalDays());
        m.put("reason",               l.getReason());
        m.put("status",               l.getStatus());
        m.put("reviewedBy",           l.getReviewedBy());
        m.put("reviewRemark",         l.getReviewRemark());
        m.put("reviewedAt",           l.getReviewedAt());
        m.put("leavesTakenInYear",    l.getLeavesTakenInYear());
        m.put("leaveDaysTakenInYear", l.getLeaveDaysTakenInYear());
        m.put("substituteTeacher",    l.getSubstituteTeacher() != null
                                          ? l.getSubstituteTeacher().getTeacherName() : null);
        m.put("createdAt",            l.getCreatedAt());
        return m;
    }

    // ── TEACHER: Apply for leave ───────────────────────────────────────────────
    @Override
    public void applyLeave(TeacherLeaveDTO dto) {

        // Improvement 1: JWT identity — teacher identified from token, not request body
        AddTeacher teacher = getLoggedInTeacher();

        // Improvement 2: Block INACTIVE teachers
        if ("INACTIVE".equalsIgnoreCase(teacher.getStatus()))
            throw new RuntimeException("Inactive teachers cannot apply for leave");

        // Validate school
        School school = schoolRepository.findBySchoolId(teacher.getSchool().getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));

        // Validate dates
        if (dto.getFromDate() == null || dto.getToDate() == null)
            throw new RuntimeException("fromDate and toDate are required");
        if (dto.getToDate().isBefore(dto.getFromDate()))
            throw new RuntimeException("toDate cannot be before fromDate");
        if (dto.getFromDate().isBefore(LocalDate.now()))
            throw new RuntimeException("Cannot apply leave for a past date");

        // Improvement 3: Overlap/conflict detection
        boolean overlaps = leaveRepository.existsOverlappingLeave(
                teacher.getId(), dto.getFromDate(), dto.getToDate());
        if (overlaps)
            throw new RuntimeException("You already have a PENDING or APPROVED leave for the overlapping dates");

        // Build entity
        TeacherLeave leave = new TeacherLeave();
        leave.setLeaveRefId(generateLeaveRefId(school.getSchoolId()));
        leave.setSchool(school);
        leave.setTeacher(teacher);
        leave.setLeaveType(dto.getLeaveType() != null ? dto.getLeaveType() : "OTHER");
        leave.setFromDate(dto.getFromDate());
        leave.setToDate(dto.getToDate());
        leave.setReason(dto.getReason());
        leave.setStatus("PENDING");

        AcademicYear ay = null;
        if (dto.getAcademicYearId() != null) {
            ay = academicYearRepository.findById(dto.getAcademicYearId())
                    .orElseThrow(() -> new RuntimeException("Academic year not found"));
        } else if (teacher.getAcademicYear() != null) {
            ay = teacher.getAcademicYear();
        }
        leave.setAcademicYear(ay);

        // Calculate approved counts at the time of leave application
        if (ay != null) {
            Long countApproved = leaveRepository.countApprovedLeavesByTeacherAndYear(
                    teacher.getId(), school.getSchoolId(), ay.getAcademicYearId());
            Integer sumApprovedDays = leaveRepository.sumApprovedDaysByTeacherAndYear(
                    teacher.getId(), school.getSchoolId(), ay.getAcademicYearId());
            leave.setLeavesTakenInYear(countApproved.intValue());
            leave.setLeaveDaysTakenInYear(sumApprovedDays);
        } else {
            Long countApproved = leaveRepository.countApprovedLeavesByTeacher(
                    teacher.getId(), school.getSchoolId());
            Integer sumApprovedDays = leaveRepository.sumApprovedDaysByTeacher(
                    teacher.getId(), school.getSchoolId());
            leave.setLeavesTakenInYear(countApproved.intValue());
            leave.setLeaveDaysTakenInYear(sumApprovedDays);
        }

        leaveRepository.save(leave);
    }

    // ── TEACHER: Cancel PENDING leave ─────────────────────────────────────────
    @Override
    public void cancelLeave(Long leaveId, Long schoolId) {

        // Improvement 1: JWT identity
        AddTeacher teacher = getLoggedInTeacher();

        TeacherLeave leave = leaveRepository.findByIdAndSchool_SchoolId(leaveId, schoolId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        // Teacher can only cancel their own leave
        if (!leave.getTeacher().getId().equals(teacher.getId()))
            throw new RuntimeException("You can only cancel your own leave request");

        if (!"PENDING".equals(leave.getStatus()))
            throw new RuntimeException("Only PENDING leaves can be cancelled");

        leave.setStatus("CANCELLED");
        leave.setReviewRemark("Cancelled by teacher");
        leaveRepository.save(leave);
    }

    // ── SCHOOL: Approve leave ─────────────────────────────────────────────────
    @Override
    public void approveLeave(TeacherLeaveDTO dto) {

        TeacherLeave leave = leaveRepository.findByIdAndSchool_SchoolId(dto.getId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        if (!"PENDING".equals(leave.getStatus()))
            throw new RuntimeException("Only PENDING leaves can be approved");

        leave.setStatus("APPROVED");
        leave.setReviewedBy(dto.getReviewedBy());
        leave.setReviewRemark(dto.getReviewRemark());
        leave.setReviewedAt(LocalDateTime.now());

        // Improvement: Substitute teacher assignment by school
        if (dto.getSubstituteTeacherId() != null) {
            AddTeacher substitute = teacherRepository.findByIdAndSchool_SchoolId(
                    dto.getSubstituteTeacherId(), dto.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("Substitute teacher not found in this school"));
            if (substitute.getId().equals(leave.getTeacher().getId()))
                throw new RuntimeException("Substitute teacher cannot be the same as the leave applicant");
            leave.setSubstituteTeacher(substitute);
        }

        leaveRepository.save(leave);

        // Improvement: Email notification to teacher on approval
        try {
            emailService.sendLeaveStatusUpdate(
                    leave.getTeacher().getEmailId(),
                    leave.getTeacher().getTeacherName(),
                    leave.getLeaveRefId(),
                    "APPROVED",
                    leave.getFromDate().toString(),
                    leave.getToDate().toString(),
                    leave.getTotalDays(),
                    dto.getReviewRemark()
            );
        } catch (Exception ignored) {
            // Email failure should not break the approval flow
        }
    }

    // ── SCHOOL: Reject leave ──────────────────────────────────────────────────
    @Override
    public void rejectLeave(TeacherLeaveDTO dto) {

        TeacherLeave leave = leaveRepository.findByIdAndSchool_SchoolId(dto.getId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Leave not found"));

        if (!"PENDING".equals(leave.getStatus()))
            throw new RuntimeException("Only PENDING leaves can be rejected");

        leave.setStatus("REJECTED");
        leave.setReviewedBy(dto.getReviewedBy());
        leave.setReviewRemark(dto.getReviewRemark());
        leave.setReviewedAt(LocalDateTime.now());
        leaveRepository.save(leave);

        // Improvement: Email notification to teacher on rejection
        try {
            emailService.sendLeaveStatusUpdate(
                    leave.getTeacher().getEmailId(),
                    leave.getTeacher().getTeacherName(),
                    leave.getLeaveRefId(),
                    "REJECTED",
                    leave.getFromDate().toString(),
                    leave.getToDate().toString(),
                    leave.getTotalDays(),
                    dto.getReviewRemark()
            );
        } catch (Exception ignored) {}
    }

    // ── TEACHER: Get own leave history ────────────────────────────────────────
    @Override
    public List<Map<String, Object>> getMyLeaves(Long teacherId, Long schoolId, String statusFilter) {
        List<TeacherLeave> list = leaveRepository.findByTeacher_IdAndSchool_SchoolId(teacherId, schoolId);
        if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL"))
            list = list.stream()
                    .filter(l -> l.getStatus().equalsIgnoreCase(statusFilter))
                    .collect(Collectors.toList());
        list.sort(Comparator.comparing(TeacherLeave::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return list.stream().map(this::toMap).collect(Collectors.toList());
    }

    // ── TEACHER: Stats summary ────────────────────────────────────────────────
    @Override
    public Map<String, Object> getMyStats(Long teacherId, Long schoolId, Long academicYearId) {
        List<TeacherLeave> all = (academicYearId != null)
                ? leaveRepository.findByTeacher_IdAndSchool_SchoolIdAndAcademicYear_AcademicYearId(teacherId, schoolId, academicYearId)
                : leaveRepository.findByTeacher_IdAndSchool_SchoolId(teacherId, schoolId);

        long totalApplied   = all.size();
        long totalApproved  = all.stream().filter(l -> "APPROVED".equals(l.getStatus())).count();
        long totalRejected  = all.stream().filter(l -> "REJECTED".equals(l.getStatus())).count();
        long totalPending   = all.stream().filter(l -> "PENDING".equals(l.getStatus())).count();
        long totalCancelled = all.stream().filter(l -> "CANCELLED".equals(l.getStatus())).count();

        int totalDaysTaken = all.stream()
                .filter(l -> "APPROVED".equals(l.getStatus()) && l.getTotalDays() != null)
                .mapToInt(TeacherLeave::getTotalDays).sum();

        // Current year approved days
        int currentYear = LocalDate.now().getYear();
        int currentYearDaysTaken = all.stream()
                .filter(l -> "APPROVED".equals(l.getStatus())
                        && l.getTotalDays() != null
                        && l.getFromDate() != null
                        && l.getFromDate().getYear() == currentYear)
                .mapToInt(TeacherLeave::getTotalDays).sum();

        // Leave breakdown by type
        Map<String, Long> byType = all.stream()
                .filter(l -> "APPROVED".equals(l.getStatus()))
                .collect(Collectors.groupingBy(TeacherLeave::getLeaveType, Collectors.counting()));

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalApplied",          totalApplied);
        stats.put("totalApproved",         totalApproved);
        stats.put("totalRejected",         totalRejected);
        stats.put("totalPending",          totalPending);
        stats.put("totalCancelled",        totalCancelled);
        stats.put("totalDaysTaken",        totalDaysTaken);
        stats.put("currentYearDaysTaken",  currentYearDaysTaken);
        stats.put("approvedLeavesByType",  byType);
        return stats;
    }

    // ── SCHOOL: Get all leaves with filters ───────────────────────────────────
    @Override
    public List<Map<String, Object>> getAll(TeacherLeaveDTO dto) {
        List<TeacherLeave> list;

        // Improvement: Date range filter
        if (dto.getFilterFromDate() != null && dto.getFilterToDate() != null) {
            list = leaveRepository.findBySchoolAndDateRange(
                    dto.getSchoolId(), dto.getFilterFromDate(), dto.getFilterToDate());
        } else if (dto.getAcademicYearId() != null) {
            list = leaveRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(
                    dto.getSchoolId(), dto.getAcademicYearId());
        } else {
            list = leaveRepository.findBySchool_SchoolId(dto.getSchoolId());
        }

        // Improvement: leaveType filter
        if (dto.getLeaveTypeFilter() != null && !dto.getLeaveTypeFilter().isBlank())
            list = list.stream()
                    .filter(l -> l.getLeaveType().equalsIgnoreCase(dto.getLeaveTypeFilter()))
                    .collect(Collectors.toList());

        // Status filter
        if (dto.getStatusFilter() != null && !dto.getStatusFilter().equalsIgnoreCase("ALL"))
            list = list.stream()
                    .filter(l -> l.getStatus().equalsIgnoreCase(dto.getStatusFilter()))
                    .collect(Collectors.toList());

        list.sort(Comparator.comparing(TeacherLeave::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return list.stream().map(this::toMap).collect(Collectors.toList());
    }

    // ── SCHOOL: Get only PENDING leaves ───────────────────────────────────────
    @Override
    public List<Map<String, Object>> getPending(Long schoolId, Long academicYearId) {
        List<TeacherLeave> list = (academicYearId != null)
                ? leaveRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(schoolId, academicYearId, "PENDING")
                : leaveRepository.findBySchool_SchoolIdAndStatus(schoolId, "PENDING");
        list.sort(Comparator.comparing(TeacherLeave::getFromDate, Comparator.nullsLast(Comparator.naturalOrder())));
        return list.stream().map(this::toMap).collect(Collectors.toList());
    }

    // ── Get single leave by ID ─────────────────────────────────────────────────
    @Override
    public Map<String, Object> getById(Long leaveId, Long schoolId) {
        TeacherLeave leave = leaveRepository.findByIdAndSchool_SchoolId(leaveId, schoolId)
                .orElseThrow(() -> new RuntimeException("Leave not found"));
        return toMap(leave);
    }

    // ── SCHOOL: Get all leaves for a specific teacher ─────────────────────────
    @Override
    public List<Map<String, Object>> getByTeacher(Long teacherId, Long schoolId, Long academicYearId) {
        List<TeacherLeave> list = (academicYearId != null)
                ? leaveRepository.findByTeacher_IdAndSchool_SchoolIdAndAcademicYear_AcademicYearId(teacherId, schoolId, academicYearId)
                : leaveRepository.findByTeacher_IdAndSchool_SchoolId(teacherId, schoolId);
        list.sort(Comparator.comparing(TeacherLeave::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return list.stream().map(this::toMap).collect(Collectors.toList());
    }

    // ── SCHOOL: School-wide stats ─────────────────────────────────────────────
    @Override
    public Map<String, Object> getSchoolStats(Long schoolId, Long academicYearId) {
        List<TeacherLeave> all = (academicYearId != null)
                ? leaveRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId)
                : leaveRepository.findBySchool_SchoolId(schoolId);

        long totalRequests  = all.size();
        long totalPending   = all.stream().filter(l -> "PENDING".equals(l.getStatus())).count();
        long totalApproved  = all.stream().filter(l -> "APPROVED".equals(l.getStatus())).count();
        long totalRejected  = all.stream().filter(l -> "REJECTED".equals(l.getStatus())).count();
        long totalCancelled = all.stream().filter(l -> "CANCELLED".equals(l.getStatus())).count();

        int totalDaysApproved = all.stream()
                .filter(l -> "APPROVED".equals(l.getStatus()) && l.getTotalDays() != null)
                .mapToInt(TeacherLeave::getTotalDays).sum();

        // Leave breakdown by type
        Map<String, Long> byType = all.stream()
                .collect(Collectors.groupingBy(TeacherLeave::getLeaveType, Collectors.counting()));

        // Improvement: Monthly trend
        Map<String, Map<String, Object>> trendMap = new LinkedHashMap<>();
        for (TeacherLeave l : all) {
            if (l.getFromDate() == null) continue;
            String monthKey = l.getFromDate().getYear() + "-"
                    + String.format("%02d", l.getFromDate().getMonthValue())
                    + " (" + capitalize(l.getFromDate().getMonth().name()) + ")";
            trendMap.computeIfAbsent(monthKey, k -> {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("month", monthKey);
                entry.put("totalRequests", 0L);
                entry.put("totalDays", 0);
                return entry;
            });
            Map<String, Object> entry = trendMap.get(monthKey);
            entry.put("totalRequests", (Long) entry.get("totalRequests") + 1);
            if (l.getTotalDays() != null)
                entry.put("totalDays", (Integer) entry.get("totalDays") + l.getTotalDays());
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalRequests",   totalRequests);
        stats.put("totalPending",    totalPending);
        stats.put("totalApproved",   totalApproved);
        stats.put("totalRejected",   totalRejected);
        stats.put("totalCancelled",  totalCancelled);
        stats.put("totalDaysApproved", totalDaysApproved);
        stats.put("leavesByType",    byType);
        stats.put("monthlyTrend",    new ArrayList<>(trendMap.values()));
        return stats;
    }

    // ── Utility ───────────────────────────────────────────────────────────────
    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}