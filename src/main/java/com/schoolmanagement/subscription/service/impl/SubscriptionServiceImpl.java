package com.schoolmanagement.subscription.service.impl;

import com.schoolmanagement.subscription.dto.*;
import com.schoolmanagement.subscription.entity.SchoolSubscription;
import com.schoolmanagement.subscription.entity.SchoolSubscription.SubscriptionStatus;
import com.schoolmanagement.subscription.entity.SubscriptionPlan;
import com.schoolmanagement.subscription.entity.SubscriptionPlan.PlanType;
import com.schoolmanagement.subscription.repository.PaymentHistoryRepository;
import com.schoolmanagement.subscription.repository.SchoolSubscriptionRepository;
import com.schoolmanagement.subscription.repository.SubscriptionPlanRepository;
import com.schoolmanagement.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final int TRIAL_DAYS = 14;        // 14 din free trial
    private static final int REMINDER_DAYS_BEFORE = 5; // 5 din pehle reminder

    private final SubscriptionPlanRepository planRepo;
    private final SchoolSubscriptionRepository subscriptionRepo;
    private final PaymentHistoryRepository paymentRepo;

    // ─── Plan Management ────────────────────────────────

    @Override
    public List<SubscriptionPlanDTO> getAllPlans() {
        return planRepo.findByIsActiveTrue()
                .stream()
                .map(this::mapPlanToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionPlanDTO getPlanByType(PlanType planType) {
        SubscriptionPlan plan = planRepo.findByPlanType(planType)
                .orElseThrow(() -> new RuntimeException("Plan not found: " + planType));
        return mapPlanToDTO(plan);
    }

    // ─── Trial Start ─────────────────────────────────────

    @Override
    @Transactional
    public SchoolSubscriptionDTO startFreeTrial(Long schoolId) {
        // Pehle check karo — koi subscription already hai?
        boolean alreadyExists = subscriptionRepo
                .findActiveSubscription(schoolId, LocalDate.now())
                .isPresent();

        if (alreadyExists) {
            throw new RuntimeException("School already has an active subscription or trial");
        }

        // BASIC plan le aao default ke liye trial mein
        SubscriptionPlan basicPlan = planRepo.findByPlanType(PlanType.BASIC)
                .orElseThrow(() -> new RuntimeException("Basic plan not configured"));

        LocalDate today = LocalDate.now();
        LocalDate trialEnd = today.plusDays(TRIAL_DAYS);

        SchoolSubscription trial = SchoolSubscription.builder()
                .schoolId(schoolId)
                .plan(basicPlan)
                .billingCycle(SchoolSubscription.BillingCycle.MONTHLY)
                .status(SubscriptionStatus.TRIAL)
                .startDate(today)
                .endDate(trialEnd)
                .trialEndDate(trialEnd)
                .isTrialPeriod(true)
                .build();

        SchoolSubscription saved = subscriptionRepo.save(trial);
        log.info("Free trial started for school: {} - Trial ends: {}", schoolId, trialEnd);

        return mapSubscriptionToDTO(saved);
    }

    // ─── Subscribe ───────────────────────────────────────

    @Override
    @Transactional
    public SchoolSubscriptionDTO subscribe(Long schoolId, SubscribeRequestDTO request) {
        SubscriptionPlan plan = planRepo.findByPlanType(request.getPlanType())
                .orElseThrow(() -> new RuntimeException("Plan not found: " + request.getPlanType()));

        LocalDate today = LocalDate.now();
        LocalDate endDate;

        // Monthly ya yearly ke hisaab se end date
        if (request.getBillingCycle() == SchoolSubscription.BillingCycle.MONTHLY) {
            endDate = today.plusMonths(1);
        } else {
            endDate = today.plusYears(1);
        }

        // Purani trial ya expired subscription deactivate karo
        subscriptionRepo.findActiveSubscription(schoolId, today)
                .ifPresent(existing -> {
                    existing.setStatus(SubscriptionStatus.CANCELLED);
                    subscriptionRepo.save(existing);
                });

        // Nayi subscription banao
        SchoolSubscription subscription = SchoolSubscription.builder()
                .schoolId(schoolId)
                .plan(plan)
                .billingCycle(request.getBillingCycle())
                .status(SubscriptionStatus.ACTIVE)
                .startDate(today)
                .endDate(endDate)
                .isTrialPeriod(false)
                .nextRenewalDate(endDate)
                .lastPaymentDate(today)
                .build();

        SchoolSubscription saved = subscriptionRepo.save(subscription);
        log.info("School {} subscribed to {} plan - Active till: {}", schoolId, plan.getPlanType(), endDate);

        return mapSubscriptionToDTO(saved);
    }

    // ─── Get Current Subscription ────────────────────────

    @Override
    public SchoolSubscriptionDTO getCurrentSubscription(Long schoolId) {
        SchoolSubscription subscription = subscriptionRepo
                .findActiveSubscription(schoolId, LocalDate.now())
                .orElseThrow(() -> new RuntimeException("No active subscription found for school: " + schoolId));
        return mapSubscriptionToDTO(subscription);
    }

    // ─── Cancel ──────────────────────────────────────────

    @Override
    @Transactional
    public SchoolSubscriptionDTO cancelSubscription(Long schoolId, String reason) {
        SchoolSubscription subscription = subscriptionRepo
                .findActiveSubscription(schoolId, LocalDate.now())
                .orElseThrow(() -> new RuntimeException("No active subscription to cancel"));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancellationReason(reason);
        subscription.setCancelledAt(java.time.LocalDateTime.now());

        SchoolSubscription saved = subscriptionRepo.save(subscription);
        log.info("Subscription cancelled for school: {} - Reason: {}", schoolId, reason);

        return mapSubscriptionToDTO(saved);
    }

    // ─── Check Access ────────────────────────────────────

    @Override
    public boolean isSubscriptionActive(Long schoolId) {
        return subscriptionRepo
                .findActiveSubscription(schoolId, LocalDate.now())
                .isPresent();
    }

    @Override
    public boolean hasFeatureAccess(Long schoolId, String featureName) {
        return subscriptionRepo
                .findActiveSubscription(schoolId, LocalDate.now())
                .map(sub -> {
                    SubscriptionPlan plan = sub.getPlan();
                    return switch (featureName) {
                        case "SMS_NOTIFICATIONS" -> plan.getSmsNotifications();
                        case "VIDEO_LECTURES"    -> plan.getVideoLectures();
                        case "STUDY_MATERIALS"   -> plan.getStudyMaterials();
                        case "ADVANCED_REPORTS"  -> plan.getAdvancedReports();
                        case "CUSTOM_BRANDING"   -> plan.getCustomBranding();
                        case "PRIORITY_SUPPORT"  -> plan.getPrioritySupport();
                        case "API_ACCESS"        -> plan.getApiAccess();
                        default -> false;
                    };
                })
                .orElse(false);
    }

    // ─── Payment History ─────────────────────────────────

    @Override
    public List<PaymentHistoryDTO> getPaymentHistory(Long schoolId) {
        return paymentRepo.findBySchoolIdOrderByCreatedAtDesc(schoolId)
                .stream()
                .map(p -> PaymentHistoryDTO.builder()
                        .id(p.getId())
                        .schoolId(p.getSchoolId())
                        .amount(p.getAmount())
                        .currency(p.getCurrency())
                        .status(p.getStatus())
                        .razorpayOrderId(p.getRazorpayOrderId())
                        .razorpayPaymentId(p.getRazorpayPaymentId())
                        .paymentMethod(p.getPaymentMethod())
                        .invoiceNumber(p.getInvoiceNumber())
                        .paidAt(p.getPaidAt())
                        .createdAt(p.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // ─── Scheduled Tasks ─────────────────────────────────

    // Roz raat 12 baje run hoga — expired subscriptions band karo
    @Scheduled(cron = "0 0 0 * * *")
    @Override
    @Transactional
    public void processExpiredSubscriptions() {
        List<SchoolSubscription> expired = subscriptionRepo
                .findExpiredSubscriptions(LocalDate.now());

        expired.forEach(sub -> {
            sub.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepo.save(sub);
            log.warn("Subscription EXPIRED for school: {}", sub.getSchoolId());
            // TODO: Email bhejo — "Aapka subscription expire ho gaya"
        });

        log.info("Processed {} expired subscriptions", expired.size());
    }

    // Roz subah 9 baje — 5 din baad expire hone walon ko reminder
    @Scheduled(cron = "0 0 9 * * *")
    @Override
    public void sendRenewalReminders() {
        LocalDate reminderDate = LocalDate.now().plusDays(REMINDER_DAYS_BEFORE);
        List<SchoolSubscription> expiringSoon = subscriptionRepo
                .findSubscriptionsExpiringOn(reminderDate);

        expiringSoon.forEach(sub -> {
            log.info("Sending renewal reminder to school: {} - Expires: {}",
                    sub.getSchoolId(), sub.getEndDate());
            // TODO: Email bhejo — "Aapka subscription 5 din mein expire hoga"
        });

        log.info("Sent renewal reminders to {} schools", expiringSoon.size());
    }

    // ─── Mappers ─────────────────────────────────────────

    private SubscriptionPlanDTO mapPlanToDTO(SubscriptionPlan plan) {
        // Yearly savings calculate karo
        java.math.BigDecimal monthlyCostForYear = plan.getMonthlyPrice()
                .multiply(java.math.BigDecimal.valueOf(12));
        java.math.BigDecimal savings = monthlyCostForYear.subtract(plan.getYearlyPrice());

        return SubscriptionPlanDTO.builder()
                .id(plan.getId())
                .planType(plan.getPlanType())
                .displayName(plan.getDisplayName())
                .description(plan.getDescription())
                .monthlyPrice(plan.getMonthlyPrice())
                .yearlyPrice(plan.getYearlyPrice())
                .maxStudents(plan.getMaxStudents())
                .maxTeachers(plan.getMaxTeachers())
                .maxStaff(plan.getMaxStaff())
                .smsNotifications(plan.getSmsNotifications())
                .videoLectures(plan.getVideoLectures())
                .studyMaterials(plan.getStudyMaterials())
                .advancedReports(plan.getAdvancedReports())
                .customBranding(plan.getCustomBranding())
                .prioritySupport(plan.getPrioritySupport())
                .apiAccess(plan.getApiAccess())
                .yearlySavings(savings.compareTo(java.math.BigDecimal.ZERO) > 0 ? savings : java.math.BigDecimal.ZERO)
                .build();
    }

    private SchoolSubscriptionDTO mapSubscriptionToDTO(SchoolSubscription sub) {
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), sub.getEndDate());
        boolean isActive = sub.getStatus() == SubscriptionStatus.ACTIVE
                || sub.getStatus() == SubscriptionStatus.TRIAL;

        return SchoolSubscriptionDTO.builder()
                .id(sub.getId())
                .schoolId(sub.getSchoolId())
                .plan(mapPlanToDTO(sub.getPlan()))
                .billingCycle(sub.getBillingCycle())
                .status(sub.getStatus())
                .startDate(sub.getStartDate())
                .endDate(sub.getEndDate())
                .trialEndDate(sub.getTrialEndDate())
                .isTrialPeriod(sub.getIsTrialPeriod())
                .nextRenewalDate(sub.getNextRenewalDate())
                .daysRemaining(Math.max(0, daysRemaining))
                .isActive(isActive)
                .createdAt(sub.getCreatedAt())
                .build();
    }
}
