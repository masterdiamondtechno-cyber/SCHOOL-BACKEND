package com.schoolmanagement.subscription;

import com.schoolmanagement.subscription.entity.SubscriptionPlan;
import com.schoolmanagement.subscription.entity.SubscriptionPlan.PlanType;
import com.schoolmanagement.subscription.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanSeeder implements CommandLineRunner {

    private final SubscriptionPlanRepository planRepo;

    @Override
    public void run(String... args) {
        // Sirf ek baar seed karo — agar already hai toh skip karo
        if (planRepo.count() > 0) {
            log.info("Subscription plans already seeded. Skipping...");
            return;
        }

        log.info("Seeding default subscription plans...");

        // ─── BASIC Plan ─────────────────────────────────
        SubscriptionPlan basic = SubscriptionPlan.builder()
                .planType(PlanType.BASIC)
                .displayName("Basic Plan")
                .description("Perfect for small schools with up to 500 students")
                .monthlyPrice(new BigDecimal("999.00"))
                .yearlyPrice(new BigDecimal("9999.00"))   // ~2 months free
                .maxStudents(500)
                .maxTeachers(30)
                .maxStaff(10)
                .smsNotifications(false)
                .videoLectures(false)
                .studyMaterials(true)
                .advancedReports(false)
                .customBranding(false)
                .prioritySupport(false)
                .apiAccess(false)
                .isActive(true)
                .build();

        // ─── PRO Plan ───────────────────────────────────
        SubscriptionPlan pro = SubscriptionPlan.builder()
                .planType(PlanType.PRO)
                .displayName("Pro Plan")
                .description("For growing schools with up to 5000 students")
                .monthlyPrice(new BigDecimal("2999.00"))
                .yearlyPrice(new BigDecimal("29999.00"))  // ~2 months free
                .maxStudents(5000)
                .maxTeachers(100)
                .maxStaff(30)
                .smsNotifications(true)
                .videoLectures(true)
                .studyMaterials(true)
                .advancedReports(true)
                .customBranding(true)
                .prioritySupport(false)
                .apiAccess(false)
                .isActive(true)
                .build();

        // ─── ENTERPRISE Plan ────────────────────────────
        SubscriptionPlan enterprise = SubscriptionPlan.builder()
                .planType(PlanType.ENTERPRISE)
                .displayName("Enterprise Plan")
                .description("For large institutions with 50,000+ students")
                .monthlyPrice(new BigDecimal("9999.00"))
                .yearlyPrice(new BigDecimal("99999.00"))  // ~2 months free
                .maxStudents(50000)
                .maxTeachers(500)
                .maxStaff(100)
                .smsNotifications(true)
                .videoLectures(true)
                .studyMaterials(true)
                .advancedReports(true)
                .customBranding(true)
                .prioritySupport(true)
                .apiAccess(true)
                .isActive(true)
                .build();

        planRepo.save(basic);
        planRepo.save(pro);
        planRepo.save(enterprise);

        log.info("✅ 3 Subscription plans seeded: BASIC (₹999/mo), PRO (₹2999/mo), ENTERPRISE (₹9999/mo)");
    }
}
