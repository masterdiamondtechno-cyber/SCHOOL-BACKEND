package com.schoolmanagement.Config;

import com.schoolmanagement.school.entity.Role;
import com.schoolmanagement.school.repository.RoleRepository;
import com.schoolmanagement.admin.entity.SubscriptionPlan;
import com.schoolmanagement.admin.repository.SubscriptionPlanRepository;
import com.schoolmanagement.admin.entity.User;
import com.schoolmanagement.admin.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * Automatically seeds the role and subscription_plans tables on application startup.
 * Only inserts if it does not already exist.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository, SubscriptionPlanRepository subscriptionPlanRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Seed Roles
        seedRole("ADMIN",   "ALL");
        seedRole("SCHOOL",  "MANAGE_SCHOOL,MANAGE_TEACHERS,MANAGE_STUDENTS,VIEW_REPORTS");
        seedRole("TEACHER", "MARK_ATTENDANCE,VIEW_STUDENTS,MANAGE_MARKS");
        seedRole("STUDENT", "VIEW_ATTENDANCE,VIEW_MARKS,VIEW_SCHEDULE");
        seedRole("PARENT",  "VIEW_CHILD_ATTENDANCE,VIEW_CHILD_MARKS");
        seedRole("OWNER",   "MANAGE_SCHOOL,VIEW_REPORTS,VIEW_FINANCE,VIEW_DASHBOARD");

        // Seed Super Admin
        seedSuperAdmin();

        // Optional cleanup of legacy seeded plans to avoid unique constraint violations
        String[] legacyPlanNames = {
            "₹4,499 Only", "₹5,999 Only", "Custom Plan",
            "₹4,499 Only (Monthly)", "₹5,999 Only (Monthly)", "Custom Plan (Monthly)",
            "₹4,499 Only (Yearly)", "₹5,999 Only (Yearly)", "Custom Plan (Yearly)",
            "Standard Plan", "Premium Plan"
        };
        for (String name : legacyPlanNames) {
            subscriptionPlanRepository.findByPlanName(name).ifPresent(p -> {
                try { subscriptionPlanRepository.delete(p); } catch (Exception e) {}
            });
        }

        // Seed Monthly Subscription Plans
        seedSubscriptionPlan(
            "Standard Plan (Monthly)",
            4499,
            "MONTHLY",
            250,
            -1,
            50,
            "Attendance & Fee Management,Exams & Progress Reports",
            "Standard Support",
            false,
            null
        );

        seedSubscriptionPlan(
            "Premium Plan (Monthly)",
            5999,
            "MONTHLY",
            500,
            -1,
            100,
            "All Standard Features,Live Lectures & Recorded Lectures,Priority Phone Support,Dedicated Mobile App",
            "24/7 Dedicated Support",
            true,
            null
        );

        seedSubscriptionPlan(
            "Custom Plan (Monthly)",
            0,
            "MONTHLY",
            -1,
            -1,
            500,
            "Tailored Features,Unlimited Scaling & Infrastructure,Dedicated Account Manager,Custom API Integration",
            "Enterprise SLA",
            false,
            null
        );

        // Seed Yearly Subscription Plans
        seedSubscriptionPlan(
            "Standard Plan (Yearly)",
            48589,
            "YEARLY",
            250,
            -1,
            50,
            "Attendance & Fee Management,Exams & Progress Reports",
            "Standard Support",
            false,
            10
        );

        seedSubscriptionPlan(
            "Premium Plan (Yearly)",
            61190,
            "YEARLY",
            500,
            -1,
            100,
            "All Standard Features,Live Lectures & Recorded Lectures,Priority Phone Support,Dedicated Mobile App",
            "24/7 Dedicated Support",
            true,
            15
        );

        seedSubscriptionPlan(
            "Custom Plan (Yearly)",
            0,
            "YEARLY",
            -1,
            -1,
            500,
            "Tailored Features,Unlimited Scaling & Infrastructure,Dedicated Account Manager,Custom API Integration",
            "Enterprise SLA",
            false,
            null
        );
    }

    private void seedRole(String name, String permissions) {
        if (!roleRepository.existsByName(name)) {
            Role role = new Role();
            role.setName(name);
            role.setPermissions(permissions);
            role.setStatus(1);
            roleRepository.save(role);
            System.out.println("✅ Role seeded: " + name);
        }
    }

    private void seedSubscriptionPlan(String planName, Integer price, String billingCycle, 
                                       Integer maxStudents, Integer maxTeachers, Integer storageLimitGb,
                                       String features, String supportLevel, Boolean isMostPopular,
                                       Integer discountPercentage) {
        SubscriptionPlan plan = subscriptionPlanRepository.findByPlanName(planName).orElse(null);
        if (plan == null) {
            plan = new SubscriptionPlan();
            plan.setPlanName(planName);
        }
        plan.setPrice(price);
        plan.setBillingCycle(billingCycle);
        plan.setMaxStudents(maxStudents);
        plan.setMaxTeachers(maxTeachers);
        plan.setStorageLimitGb(storageLimitGb);
        plan.setFeatures(features);
        plan.setSupportLevel(supportLevel);
        plan.setIsMostPopular(isMostPopular);
        plan.setDiscountPercentage(discountPercentage);
        plan.setStatus("ACTIVE");
        subscriptionPlanRepository.save(plan);
        System.out.println("✅ Subscription plan seeded/updated: " + planName);
    }

    private void seedSuperAdmin() {
        if (!userRepository.existsByUsername("superadmin") && !userRepository.existsByEmail("superadmin@system.com")) {
            User superAdmin = new User();
            superAdmin.setUsername("superadmin");
            superAdmin.setEmail("superadmin@system.com");
            superAdmin.setPassword(passwordEncoder.encode("Admin@123"));
            superAdmin.setFullName("Super Admin");
            superAdmin.setStatus("ACTIVE");
            superAdmin.setPhone("1234567890");
            superAdmin.setCreatedAt(LocalDateTime.now());
            superAdmin.setCreatedTimestamp(LocalDateTime.now());
            superAdmin.setUpdatedAt(LocalDateTime.now());
            superAdmin.setUpdatedTimestamp(LocalDateTime.now());

            roleRepository.findByName("ADMIN").ifPresent(superAdmin::setRole);

            userRepository.save(superAdmin);
            System.out.println("✅ Default Super Admin seeded successfully: superadmin / superadmin@system.com");
        }
    }
}
