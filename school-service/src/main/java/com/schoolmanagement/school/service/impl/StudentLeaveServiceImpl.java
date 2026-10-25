package com.schoolmanagement.school.serviceImpl;

import com.schoolmanagement.school.dto.StudentLeaveDTO;
import com.schoolmanagement.school.entity.AcademicYear;
import com.schoolmanagement.school.entity.AddStudent;
import com.schoolmanagement.school.entity.AddStandard;
import com.schoolmanagement.school.entity.School;
import com.schoolmanagement.school.entity.StudentLeave;
import com.schoolmanagement.school.entity.AddTeacher;
import com.schoolmanagement.school.repository.AddTeacherRepository;
import com.schoolmanagement.school.repository.AcademicYearRepository;
import com.schoolmanagement.school.repository.AddStudentRepository;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.school.repository.StudentLeaveRepository;
import com.schoolmanagement.school.service.EmailService;
import com.schoolmanagement.school.service.StudentLeaveService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StudentLeaveServiceImpl implements StudentLeaveService {

    private final StudentLeaveRepository leaveRepository;
    private final AddStudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AddTeacherRepository teacherRepository;
    private final EmailService emailService;

    public StudentLeaveServiceImpl(StudentLeaveRepository leaveRepository,
                                   AddStudentRepository studentRepository,
                                   SchoolRepository schoolRepository,
                                   AcademicYearRepository academicYearRepository,
                                   AddTeacherRepository teacherRepository,
                                   EmailService emailService) {
        this.leaveRepository = leaveRepository;
        this.studentRepository = studentRepository;
        this.schoolRepository = schoolRepository;
        this.academicYearRepository = academicYearRepository;
        this.teacherRepository = teacherRepository;
        this.emailService = emailService;
    }

    // ── JWT helper: get logged-in student from SecurityContext ─────────────────
    private AddStudent getLoggedInStudent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new RuntimeException("Unauthorized");
        String username = auth.getName();
        return studentRepository.findByEmailIdOrMobileNumber(username, username)
                .orElseThrow(() -> new RuntimeException("Authenticated student not found"));
    }

    // ── JWT helper: get logged-in teacher from SecurityContext ─────────────────
    private AddTeacher getLoggedInTeacher() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new RuntimeException("Unauthorized");
        String username = auth.getName();
        return teacherRepository.findByEmailIdOrMobileNumber(username, username)
                .orElseThrow(() -> new RuntimeException("Authenticated teacher not found"));
    }

    // ── Auto-generate leave ref ID: ST-LV-YYYY-001 ──────────────────────────────
    private String generateLeaveRefId(Long schoolId) {
        int year = LocalDate.now().getYear();
        String prefix = "ST-LV-" + year + "-";
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
    private Map<String, Object> toMap(StudentLeave l) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",                   l.getId());
        m.put("leaveRefId",           l.getLeaveRefId());
        m.put("studentId",            l.getStudent().getId());
        m.put("studentName",          l.getStudent().getFirstName() + " " + l.getStudent().getLastName());
        m.put("leaveType",            l.getLeaveType());
        m.put("fromDate",             l.getFromDate());
        m.put("toDate",               l.getToDate());
        m.put("totalDays",            l.getTotalDays());
        m.put("reason",               l.getReason());
        m.put("status",               l.getStatus());
        m.put("reviewedBy",           l.getReviewedBy());
        m.put("reviewRemark",         l.getReviewRemark());
        m.put("reviewedAt",           l.getReviewedAt());
        m.put("createdAt",            l.getCreatedAt());
        return m;
    }

    // ── STUDENT: Apply for leave ───────────────────────────────────────────────
    @Override
    public void applyLeave(StudentLeaveDTO dto) {
        AddStudent student = getLoggedInStudent();

        if ("INACTIVE".equalsIgnoreCase(student.getStatus()))
            throw new RuntimeException("Inactive students cannot apply for leave");

        School school = schoolRepository.findBySchoolId(student.getSchool().getSchoolId())
                .orElseThrow(() -> new RuntimeException("School not found"));

        if (dto.getFromDate() == null || dto.getToDate() == null)
            throw new RuntimeException("fromDate and toDate are required");
        if (dto.getToDate().isBefore(dto.getFromDate()))
            throw new RuntimeException("toDate cannot be before fromDate");
        if (dto.getFromDate().isBefore(LocalDate.now()))
            throw new RuntimeException("Cannot apply leave for a past date");

        boolean overlaps = leaveRepository.existsOverlappingLeave(
                student.getId(), dto.getFromDate(), dto.getToDate());
        if (overlaps)
            throw new RuntimeException("You already have a PENDING or APPROVED leave for the overlapping dates");

        StudentLeave leave = new StudentLeave();
        leave.setLeaveRefId(generateLeaveRefId(school.getSchoolId()));
        leave.setSchool(school);
        leave.setStudent(student);
        leave.setLeaveType(dto.getLeaveType() != null ? dto.getLeaveType() : "OTHER");
        leave.setFromDate(dto.getFromDate());
        leave.setToDate(dto.getToDate());
        leave.setReason(dto.getReason());
        leave.setStatus("PENDING");

        if (dto.getAcademicYearId() != null) {
            AcademicYear ay = academicYearRepository.findById(dto.getAcademicYearId())
                    .orElseThrow(() -> new RuntimeException("Academic year not found"));
            leave.setAcademicYear(ay);
        } else if (student.getAcademicYear() != null) {
            leave.setAcademicYear(student.getAcademicYear());
        }

        leaveRepository.save(leave);
    }

    // ── STUDENT: Cancel PENDING leave ─────────────────────────────────────────
    @Override
    public void cancelLeave(Long leaveId, Long schoolId) {
        AddStudent student = getLoggedInStudent();

        StudentLeave leave = leaveRepository.findByIdAndSchool_SchoolId(leaveId, schoolId)
                .orElseThrow(() -> new RuntimeException("Leave record not found"));

        if (!leave.getStudent().getId().equals(student.getId()))
            throw new RuntimeException("You can only cancel your own leave request");

        if (!"PENDING".equals(leave.getStatus()))
            throw new RuntimeException("Only PENDING leaves can be cancelled");

        leave.setStatus("CANCELLED");
        leave.setReviewRemark("Cancelled by student");
        leaveRepository.save(leave);
    }

    // ── SCHOOL: Approve leave ─────────────────────────────────────────────────
    @Override
    public void approveLeave(StudentLeaveDTO dto) {
        StudentLeave leave = leaveRepository.findByIdAndSchool_SchoolId(dto.getId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Leave record not found"));

        if (!"PENDING".equals(leave.getStatus()))
            throw new RuntimeException("Only PENDING leaves can be approved");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isTeacher = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
        if (isTeacher) {
            String username = auth.getName();
            AddTeacher teacher = teacherRepository.findByEmailIdOrMobileNumber(username, username)
                    .orElseThrow(() -> new RuntimeException("Authenticated teacher not found"));
            AddStandard studentStd = leave.getStudent().getAddStandard();
            if (studentStd == null || studentStd.getClassTeacher() == null || !studentStd.getClassTeacher().getId().equals(teacher.getId())) {
                throw new RuntimeException("You are not the class teacher for this student");
            }
            if (dto.getReviewedBy() == null || dto.getReviewedBy().isBlank()) {
                dto.setReviewedBy("Class Teacher: " + teacher.getTeacherName());
            }
        }

        leave.setStatus("APPROVED");
        leave.setReviewedBy(dto.getReviewedBy());
        leave.setReviewRemark(dto.getReviewRemark());
        leave.setReviewedAt(LocalDateTime.now());

        leaveRepository.save(leave);

        // Email notification to parent on approval
        try {
            if (leave.getStudent().getParentEmailId() != null && !leave.getStudent().getParentEmailId().isBlank()) {
                emailService.sendStudentLeaveStatusUpdate(
                        leave.getStudent().getParentEmailId(),
                        leave.getStudent().getGuardianName(),
                        leave.getStudent().getFirstName() + " " + leave.getStudent().getLastName(),
                        leave.getLeaveRefId(),
                        "APPROVED",
                        leave.getFromDate().toString(),
                        leave.getToDate().toString(),
                        leave.getTotalDays(),
                        dto.getReviewRemark()
                );
            }
        } catch (Exception ignored) {}
    }

    // ── SCHOOL: Reject leave ──────────────────────────────────────────────────
    @Override
    public void rejectLeave(StudentLeaveDTO dto) {
        StudentLeave leave = leaveRepository.findByIdAndSchool_SchoolId(dto.getId(), dto.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Leave record not found"));

        if (!"PENDING".equals(leave.getStatus()))
            throw new RuntimeException("Only PENDING leaves can be rejected");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isTeacher = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
        if (isTeacher) {
            String username = auth.getName();
            AddTeacher teacher = teacherRepository.findByEmailIdOrMobileNumber(username, username)
                    .orElseThrow(() -> new RuntimeException("Authenticated teacher not found"));
            AddStandard studentStd = leave.getStudent().getAddStandard();
            if (studentStd == null || studentStd.getClassTeacher() == null || !studentStd.getClassTeacher().getId().equals(teacher.getId())) {
                throw new RuntimeException("You are not the class teacher for this student");
            }
            if (dto.getReviewedBy() == null || dto.getReviewedBy().isBlank()) {
                dto.setReviewedBy("Class Teacher: " + teacher.getTeacherName());
            }
        }

        leave.setStatus("REJECTED");
        leave.setReviewedBy(dto.getReviewedBy());
        leave.setReviewRemark(dto.getReviewRemark());
        leave.setReviewedAt(LocalDateTime.now());

        leaveRepository.save(leave);

        // Email notification to parent on rejection
        try {
            if (leave.getStudent().getParentEmailId() != null && !leave.getStudent().getParentEmailId().isBlank()) {
                emailService.sendStudentLeaveStatusUpdate(
                        leave.getStudent().getParentEmailId(),
                        leave.getStudent().getGuardianName(),
                        leave.getStudent().getFirstName() + " " + leave.getStudent().getLastName(),
                        leave.getLeaveRefId(),
                        "REJECTED",
                        leave.getFromDate().toString(),
                        leave.getToDate().toString(),
                        leave.getTotalDays(),
                        dto.getReviewRemark()
                );
            }
        } catch (Exception ignored) {}
    }

    // ── STUDENT: Get own leave history ────────────────────────────────────────
    @Override
    public List<Map<String, Object>> getMyLeaves(Long studentId, Long schoolId, String statusFilter) {
        List<StudentLeave> list = leaveRepository.findByStudent_IdAndSchool_SchoolId(studentId, schoolId);
        if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL"))
            list = list.stream()
                    .filter(l -> l.getStatus().equalsIgnoreCase(statusFilter))
                    .collect(Collectors.toList());
        list.sort(Comparator.comparing(StudentLeave::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return list.stream().map(this::toMap).collect(Collectors.toList());
    }

    // ── STUDENT: Stats summary ────────────────────────────────────────────────
    @Override
    public Map<String, Object> getMyStats(Long studentId, Long schoolId, Long academicYearId) {
        List<StudentLeave> all = (academicYearId != null)
                ? leaveRepository.findByStudent_IdAndSchool_SchoolIdAndAcademicYear_AcademicYearId(studentId, schoolId, academicYearId)
                : leaveRepository.findByStudent_IdAndSchool_SchoolId(studentId, schoolId);

        long totalApplied   = all.size();
        long totalApproved  = all.stream().filter(l -> "APPROVED".equals(l.getStatus())).count();
        long totalRejected  = all.stream().filter(l -> "REJECTED".equals(l.getStatus())).count();
        long totalPending   = all.stream().filter(l -> "PENDING".equals(l.getStatus())).count();
        long totalCancelled = all.stream().filter(l -> "CANCELLED".equals(l.getStatus())).count();

        int totalDaysTaken = all.stream()
                .filter(l -> "APPROVED".equals(l.getStatus()) && l.getTotalDays() != null)
                .mapToInt(StudentLeave::getTotalDays).sum();

        int currentYear = LocalDate.now().getYear();
        int currentYearDaysTaken = all.stream()
                .filter(l -> "APPROVED".equals(l.getStatus())
                        && l.getTotalDays() != null
                        && l.getFromDate() != null
                        && l.getFromDate().getYear() == currentYear)
                .mapToInt(StudentLeave::getTotalDays).sum();

        Map<String, Long> byType = all.stream()
                .filter(l -> "APPROVED".equals(l.getStatus()))
                .collect(Collectors.groupingBy(StudentLeave::getLeaveType, Collectors.counting()));

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
    public List<Map<String, Object>> getAll(StudentLeaveDTO dto) {
        List<StudentLeave> list;

        if (dto.getFilterFromDate() != null && dto.getFilterToDate() != null) {
            list = leaveRepository.findBySchoolAndDateRange(
                    dto.getSchoolId(), dto.getFilterFromDate(), dto.getFilterToDate());
        } else if (dto.getAcademicYearId() != null) {
            list = leaveRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(
                    dto.getSchoolId(), dto.getAcademicYearId());
        } else {
            list = leaveRepository.findBySchool_SchoolId(dto.getSchoolId());
        }

        if (dto.getLeaveTypeFilter() != null && !dto.getLeaveTypeFilter().isBlank())
            list = list.stream()
                    .filter(l -> l.getLeaveType().equalsIgnoreCase(dto.getLeaveTypeFilter()))
                    .collect(Collectors.toList());

        if (dto.getStatusFilter() != null && !dto.getStatusFilter().equalsIgnoreCase("ALL"))
            list = list.stream()
                    .filter(l -> l.getStatus().equalsIgnoreCase(dto.getStatusFilter()))
                    .collect(Collectors.toList());

        list.sort(Comparator.comparing(StudentLeave::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return list.stream().map(this::toMap).collect(Collectors.toList());
    }

    // ── SCHOOL: Get only PENDING leaves ───────────────────────────────────────
    @Override
    public List<Map<String, Object>> getPending(Long schoolId, Long academicYearId) {
        List<StudentLeave> list = (academicYearId != null)
                ? leaveRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearIdAndStatus(schoolId, academicYearId, "PENDING")
                : leaveRepository.findBySchool_SchoolIdAndStatus(schoolId, "PENDING");
        list.sort(Comparator.comparing(StudentLeave::getFromDate, Comparator.nullsLast(Comparator.naturalOrder())));
        return list.stream().map(this::toMap).collect(Collectors.toList());
    }

    // ── Get single leave by ID ─────────────────────────────────────────────────
    @Override
    public Map<String, Object> getById(Long leaveId, Long schoolId) {
        StudentLeave leave = leaveRepository.findByIdAndSchool_SchoolId(leaveId, schoolId)
                .orElseThrow(() -> new RuntimeException("Leave record not found"));
        return toMap(leave);
    }

    // ── SCHOOL: Get all leaves for a specific student ─────────────────────────
    @Override
    public List<Map<String, Object>> getByStudent(Long studentId, Long schoolId, Long academicYearId) {
        List<StudentLeave> list = (academicYearId != null)
                ? leaveRepository.findByStudent_IdAndSchool_SchoolIdAndAcademicYear_AcademicYearId(studentId, schoolId, academicYearId)
                : leaveRepository.findByStudent_IdAndSchool_SchoolId(studentId, schoolId);
        list.sort(Comparator.comparing(StudentLeave::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return list.stream().map(this::toMap).collect(Collectors.toList());
    }

    // ── SCHOOL: School-wide stats ─────────────────────────────────────────────
    @Override
    public Map<String, Object> getSchoolStats(Long schoolId, Long academicYearId) {
        List<StudentLeave> all = (academicYearId != null)
                ? leaveRepository.findBySchool_SchoolIdAndAcademicYear_AcademicYearId(schoolId, academicYearId)
                : leaveRepository.findBySchool_SchoolId(schoolId);

        long totalRequests  = all.size();
        long totalPending   = all.stream().filter(l -> "PENDING".equals(l.getStatus())).count();
        long totalApproved  = all.stream().filter(l -> "APPROVED".equals(l.getStatus())).count();
        long totalRejected  = all.stream().filter(l -> "REJECTED".equals(l.getStatus())).count();
        long totalCancelled = all.stream().filter(l -> "CANCELLED".equals(l.getStatus())).count();

        int totalDaysApproved = all.stream()
                .filter(l -> "APPROVED".equals(l.getStatus()) && l.getTotalDays() != null)
                .mapToInt(StudentLeave::getTotalDays).sum();

        Map<String, Long> byType = all.stream()
                .collect(Collectors.groupingBy(StudentLeave::getLeaveType, Collectors.counting()));

        Map<String, Map<String, Object>> trendMap = new LinkedHashMap<>();
        for (StudentLeave l : all) {
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

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    @Override
    public List<Map<String, Object>> getClassStudentLeaves(Long schoolId, String statusFilter) {
        AddTeacher teacher = getLoggedInTeacher();
        List<StudentLeave> list = leaveRepository.findByClassTeacherAndSchool(teacher.getId(), schoolId);

        if (statusFilter != null && !statusFilter.equalsIgnoreCase("ALL")) {
            list = list.stream()
                    .filter(l -> l.getStatus().equalsIgnoreCase(statusFilter))
                    .collect(Collectors.toList());
        }

        list.sort(Comparator.comparing(StudentLeave::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return list.stream().map(this::toMap).collect(Collectors.toList());
    }
}
