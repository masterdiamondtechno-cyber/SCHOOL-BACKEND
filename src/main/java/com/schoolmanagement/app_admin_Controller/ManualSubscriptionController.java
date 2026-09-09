package com.schoolmanagement.app_admin_Controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.schoolmanagement.app_admin_DTO.ManualSubscriptionRequestDTO;
import com.schoolmanagement.app_admin_Entity.SubscriptionAssignmentHistory;
import com.schoolmanagement.app_admin_Entity.SubscriptionPlan;
import com.schoolmanagement.app_admin_Entity.User;
import com.schoolmanagement.app_admin_Repo.SubscriptionAssignmentHistoryRepository;
import com.schoolmanagement.app_admin_Repo.SubscriptionPlanRepository;
import com.schoolmanagement.app_admin_Repo.UserRepository;
import com.schoolmanagement.app_admin_Service.SubscriptionPlanService;
import com.schoolmanagement.school_admin_Entity.School;
import com.schoolmanagement.school_admin_Repo.SchoolRepository;

@RestController
@RequestMapping("/api/app_admin/manual-subscriptions")
public class ManualSubscriptionController {

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private SubscriptionAssignmentHistoryRepository historyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionPlanService subscriptionPlanService;

    @GetMapping("/plans")
    public ResponseEntity<Map<String, Object>> getAvailablePlans() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 200);
        response.put("status", "SUCCESS");
        response.put("message", "Active subscription plans retrieved");
        response.put("data", subscriptionPlanService.getAllActivePlans());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign")
    public ResponseEntity<Map<String, Object>> assignSubscription(@RequestBody ManualSubscriptionRequestDTO request) {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            School school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("School not found"));

            SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                    .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

            // 1. Capture Previous State
            String prevState = buildSubscriptionStateDescription(school);

            // 2. Parse validity dates
            LocalDateTime start = parseDateTime(request.getStartDate());
            LocalDateTime end = parseDateTime(request.getEndDate());

            // Handle deactivation via status toggle
            if ("INACTIVE".equalsIgnoreCase(request.getStatus())) {
                end = LocalDateTime.now().minusSeconds(1); // Make it expired immediately
            }

            // 3. Update School details
            school.setSubscriptionSource("MANUAL");
            school.setSubscriptionPlan(plan);
            school.setSubscriptionStatus(plan.getPlanName());
            school.setSubscriptionStartDate(start);
            school.setSubscriptionEndDate(end);
            school.setSubscriptionDuration(request.getDuration());
            school.setAssignedBy(request.getAssignedBy());
            school.setAssignmentReason(request.getReason());
            school.setManualAssignmentDate(LocalDateTime.now());
            school.setTrialEndDate(null);

            schoolRepository.save(school);

            // 4. Resolve admin name
            String adminName = getAdminName(request.getAssignedBy());

            // 5. Create History/Audit Log
            SubscriptionAssignmentHistory history = new SubscriptionAssignmentHistory();
            history.setSchoolId(school.getSchoolId());
            history.setPlanId(plan.getPlanId());
            history.setPlanName(plan.getPlanName());
            history.setSubscriptionSource("MANUAL");
            history.setAssignedBy(request.getAssignedBy());
            history.setAssignedByName(adminName);
            history.setAssignmentDate(LocalDateTime.now());
            history.setStartDate(start);
            history.setEndDate(end);
            history.setDuration(request.getDuration());
            history.setReason(request.getReason());
            history.setPreviousSubscriptionState(prevState);
            history.setNewSubscriptionState(buildSubscriptionStateDescription(school));

            historyRepository.save(history);

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "Subscription assigned manually successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", 400);
            response.put("status", "FAILED");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/extend")
    public ResponseEntity<Map<String, Object>> extendSubscription(@RequestBody ManualSubscriptionRequestDTO request) {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            School school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("School not found"));

            String prevState = buildSubscriptionStateDescription(school);
            LocalDateTime newEnd = parseDateTime(request.getEndDate());

            school.setSubscriptionEndDate(newEnd);
            if (request.getDuration() != null) {
                school.setSubscriptionDuration(request.getDuration());
            }
            school.setAssignmentReason(request.getReason());
            school.setManualAssignmentDate(LocalDateTime.now());
            schoolRepository.save(school);

            String adminName = getAdminName(request.getAssignedBy());

            SubscriptionAssignmentHistory history = new SubscriptionAssignmentHistory();
            history.setSchoolId(school.getSchoolId());
            if (school.getSubscriptionPlan() != null) {
                history.setPlanId(school.getSubscriptionPlan().getPlanId());
                history.setPlanName(school.getSubscriptionPlan().getPlanName());
            } else {
                history.setPlanName(school.getSubscriptionStatus());
            }
            history.setSubscriptionSource(school.getSubscriptionSource());
            history.setAssignedBy(request.getAssignedBy());
            history.setAssignedByName(adminName);
            history.setAssignmentDate(LocalDateTime.now());
            history.setStartDate(school.getSubscriptionStartDate());
            history.setEndDate(newEnd);
            history.setDuration(school.getSubscriptionDuration());
            history.setReason("Extended: " + request.getReason());
            history.setPreviousSubscriptionState(prevState);
            history.setNewSubscriptionState(buildSubscriptionStateDescription(school));

            historyRepository.save(history);

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "Subscription extended successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", 400);
            response.put("status", "FAILED");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/update-validity")
    public ResponseEntity<Map<String, Object>> updateValidity(@RequestBody ManualSubscriptionRequestDTO request) {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            School school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("School not found"));

            String prevState = buildSubscriptionStateDescription(school);
            LocalDateTime start = parseDateTime(request.getStartDate());
            LocalDateTime end = parseDateTime(request.getEndDate());

            school.setSubscriptionStartDate(start);
            school.setSubscriptionEndDate(end);
            if (request.getDuration() != null) {
                school.setSubscriptionDuration(request.getDuration());
            }
            school.setAssignmentReason(request.getReason());
            school.setManualAssignmentDate(LocalDateTime.now());
            schoolRepository.save(school);

            String adminName = getAdminName(request.getAssignedBy());

            SubscriptionAssignmentHistory history = new SubscriptionAssignmentHistory();
            history.setSchoolId(school.getSchoolId());
            if (school.getSubscriptionPlan() != null) {
                history.setPlanId(school.getSubscriptionPlan().getPlanId());
                history.setPlanName(school.getSubscriptionPlan().getPlanName());
            } else {
                history.setPlanName(school.getSubscriptionStatus());
            }
            history.setSubscriptionSource(school.getSubscriptionSource());
            history.setAssignedBy(request.getAssignedBy());
            history.setAssignedByName(adminName);
            history.setAssignmentDate(LocalDateTime.now());
            history.setStartDate(start);
            history.setEndDate(end);
            history.setDuration(school.getSubscriptionDuration());
            history.setReason("Validity Updated: " + request.getReason());
            history.setPreviousSubscriptionState(prevState);
            history.setNewSubscriptionState(buildSubscriptionStateDescription(school));

            historyRepository.save(history);

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "Validity dates updated successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", 400);
            response.put("status", "FAILED");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/revoke")
    public ResponseEntity<Map<String, Object>> revokeSubscription(@RequestBody ManualSubscriptionRequestDTO request) {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            School school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("School not found"));

            String prevState = buildSubscriptionStateDescription(school);

            // Set end date to now (expired) and source to manual
            school.setSubscriptionSource("MANUAL");
            school.setSubscriptionEndDate(LocalDateTime.now().minusSeconds(1));
            school.setAssignmentReason("Revoked: " + request.getReason());
            school.setManualAssignmentDate(LocalDateTime.now());
            schoolRepository.save(school);

            String adminName = getAdminName(request.getAssignedBy());

            SubscriptionAssignmentHistory history = new SubscriptionAssignmentHistory();
            history.setSchoolId(school.getSchoolId());
            if (school.getSubscriptionPlan() != null) {
                history.setPlanId(school.getSubscriptionPlan().getPlanId());
                history.setPlanName(school.getSubscriptionPlan().getPlanName());
            } else {
                history.setPlanName(school.getSubscriptionStatus());
            }
            history.setSubscriptionSource("MANUAL");
            history.setAssignedBy(request.getAssignedBy());
            history.setAssignedByName(adminName);
            history.setAssignmentDate(LocalDateTime.now());
            history.setStartDate(school.getSubscriptionStartDate());
            history.setEndDate(school.getSubscriptionEndDate());
            history.setDuration(school.getSubscriptionDuration());
            history.setReason("Revoked: " + request.getReason());
            history.setPreviousSubscriptionState(prevState);
            history.setNewSubscriptionState(buildSubscriptionStateDescription(school));

            historyRepository.save(history);

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "Subscription revoked successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", 400);
            response.put("status", "FAILED");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/history/{schoolId}")
    public ResponseEntity<Map<String, Object>> getHistory(@PathVariable Long schoolId) {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            School school = schoolRepository.findById(schoolId)
                    .orElseThrow(() -> new RuntimeException("School not found"));

            List<SubscriptionAssignmentHistory> logs = historyRepository.findBySchoolIdOrderByIdDesc(schoolId);

            // Synthesize standard trial entry if empty or as the initial entry
            boolean hasTrial = logs.stream().anyMatch(l -> "TRIAL".equalsIgnoreCase(l.getSubscriptionSource()));
            if (!hasTrial) {
                SubscriptionAssignmentHistory trialLog = new SubscriptionAssignmentHistory();
                trialLog.setId(0L); // Synthetic ID
                trialLog.setSchoolId(school.getSchoolId());
                trialLog.setPlanName("Standard Plan (Monthly)");
                trialLog.setSubscriptionSource("TRIAL");
                trialLog.setAssignmentDate(school.getCreatedAt() != null ? school.getCreatedAt() : LocalDateTime.now().minusDays(7));
                trialLog.setStartDate(school.getCreatedAt() != null ? school.getCreatedAt() : LocalDateTime.now().minusDays(7));
                trialLog.setEndDate(school.getTrialEndDate() != null ? school.getTrialEndDate() : LocalDateTime.now());
                trialLog.setDuration("7 Days");
                trialLog.setReason("Auto-assigned upon school registration");
                trialLog.setAssignedByName("System");
                
                // Add to the list
                List<SubscriptionAssignmentHistory> completeLogs = new ArrayList<>();
                completeLogs.addAll(logs);
                completeLogs.add(trialLog); // Add at the end (oldest)
                logs = completeLogs;
            }

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "Subscription history logs retrieved");
            response.put("data", logs);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("code", 400);
            response.put("status", "FAILED");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            if (dateStr.contains("T")) {
                return LocalDateTime.parse(dateStr);
            } else {
                return java.time.LocalDate.parse(dateStr).atStartOfDay();
            }
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private String getAdminName(Long adminId) {
        if (adminId == null) {
            return "Super Admin";
        }
        Optional<User> userOpt = userRepository.findById(adminId);
        return userOpt.map(User::getFullName).orElse("Super Admin");
    }

    private String buildSubscriptionStateDescription(School school) {
        String source = school.getSubscriptionSource() != null ? school.getSubscriptionSource() : "TRIAL";
        String plan = school.getSubscriptionStatus() != null ? school.getSubscriptionStatus() : "Standard Plan (Monthly)";
        LocalDateTime exp = "TRIAL".equalsIgnoreCase(source) ? school.getTrialEndDate() : school.getSubscriptionEndDate();
        boolean expired = school.isSubscriptionExpired();
        return "Source: " + source + ", Plan: " + plan + ", Expiry: " + (exp != null ? exp.toString() : "N/A") + ", Status: " + (expired ? "EXPIRED" : "ACTIVE");
    }
}
