package com.schoolmanagement.subscription.service;

import com.schoolmanagement.subscription.dto.*;
import com.schoolmanagement.subscription.entity.SubscriptionPlan.PlanType;

import java.util.List;

public interface SubscriptionService {

    // ─── Plan Management (Admin ke liye) ────────────────
    List<SubscriptionPlanDTO> getAllPlans();
    SubscriptionPlanDTO getPlanByType(PlanType planType);

    // ─── School Subscription ────────────────────────────

    // Naya school register kare → Trial shuru karo
    SchoolSubscriptionDTO startFreeTrial(Long schoolId);

    // School plan subscribe kare
    SchoolSubscriptionDTO subscribe(Long schoolId, SubscribeRequestDTO request);

    // School ka current subscription dekho
    SchoolSubscriptionDTO getCurrentSubscription(Long schoolId);

    // Subscription cancel karo
    SchoolSubscriptionDTO cancelSubscription(Long schoolId, String reason);

    // ─── Subscription Check ─────────────────────────────

    // School ka subscription valid hai?
    boolean isSubscriptionActive(Long schoolId);

    // School ke plan mein ye feature available hai?
    boolean hasFeatureAccess(Long schoolId, String featureName);

    // ─── Payment ────────────────────────────────────────

    // School ki payment history
    List<PaymentHistoryDTO> getPaymentHistory(Long schoolId);

    // ─── Scheduled Tasks (Auto runs daily) ──────────────

    // Expired subscriptions band karo
    void processExpiredSubscriptions();

    // 5 din pehle reminder email bhejo
    void sendRenewalReminders();
}
