package com.schoolmanagement.subscription.service.impl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.schoolmanagement.school.repository.SchoolRepository;
import com.schoolmanagement.subscription.dto.PaymentOrderRequestDTO;
import com.schoolmanagement.subscription.dto.PaymentOrderResponseDTO;
import com.schoolmanagement.subscription.dto.PaymentVerifyRequestDTO;
import com.schoolmanagement.subscription.dto.SchoolSubscriptionDTO;
import com.schoolmanagement.subscription.dto.SubscriptionPlanDTO;
import com.schoolmanagement.subscription.entity.PaymentHistory;
import com.schoolmanagement.subscription.entity.PaymentHistory.PaymentStatus;
import com.schoolmanagement.subscription.entity.SchoolSubscription;
import com.schoolmanagement.subscription.entity.SchoolSubscription.BillingCycle;
import com.schoolmanagement.subscription.entity.SchoolSubscription.SubscriptionStatus;
import com.schoolmanagement.subscription.entity.SubscriptionPlan;
import com.schoolmanagement.subscription.repository.PaymentHistoryRepository;
import com.schoolmanagement.subscription.repository.SchoolSubscriptionRepository;
import com.schoolmanagement.subscription.repository.SubscriptionPlanRepository;
import com.schoolmanagement.subscription.service.RazorpayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RazorpayServiceImpl implements RazorpayService {

    private final SubscriptionPlanRepository planRepository;
    private final SchoolSubscriptionRepository subscriptionRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final SchoolRepository schoolRepository;

    @Value("${razorpay.key.id:rzp_test_dummy_key_id}")
    private String keyId;

    @Value("${razorpay.key.secret:dummy_secret_key}")
    private String keySecret;

    @Value("${razorpay.mock.enabled:true}")
    private boolean mockEnabled;

    /**
     * Check karta hai ki Razorpay actual API call karega ya Dummy/Mock simulate karega.
     */
    private boolean isSimulatedMode() {
        return mockEnabled || keyId == null || keyId.startsWith("rzp_test_dummy");
    }

    @Override
    @Transactional
    public PaymentOrderResponseDTO createOrder(Long schoolId, PaymentOrderRequestDTO request) {
        SubscriptionPlan plan = planRepository.findByPlanType(request.getPlanType())
                .orElseThrow(() -> new RuntimeException("Plan not found: " + request.getPlanType()));

        BigDecimal amount = (request.getBillingCycle() == BillingCycle.MONTHLY)
                ? plan.getMonthlyPrice()
                : plan.getYearlyPrice();

        // Razorpay accepts amount in paise (1 INR = 100 Paise)
        long amountInPaise = amount.multiply(BigDecimal.valueOf(100)).longValue();
        String orderId;

        if (isSimulatedMode()) {
            // Mock Mode: Generate dummy order ID for testing
            orderId = "order_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);
            log.info("🧪 [MOCK MODE] Generated fake Razorpay order: {} for school: {} | Amount: ₹{}",
                    orderId, schoolId, amount);
        } else {
            // Real Mode: Call official Razorpay API
            try {
                RazorpayClient client = new RazorpayClient(keyId, keySecret);
                JSONObject orderReq = new JSONObject();
                orderReq.put("amount", amountInPaise);
                orderReq.put("currency", "INR");
                orderReq.put("receipt", "rcpt_" + schoolId + "_" + System.currentTimeMillis());

                Order order = client.orders.create(orderReq);
                orderId = order.get("id");
                log.info("💳 [REAL MODE] Razorpay order created: {} for school: {}", orderId, schoolId);
            } catch (Exception e) {
                log.error("Failed to create Razorpay order: {}", e.getMessage(), e);
                throw new RuntimeException("Payment Gateway Error: " + e.getMessage());
            }
        }

        return PaymentOrderResponseDTO.builder()
                .orderId(orderId)
                .schoolId(schoolId)
                .planType(plan.getPlanType())
                .billingCycle(request.getBillingCycle())
                .amount(amount)
                .amountInPaise(amountInPaise)
                .currency("INR")
                .razorpayKeyId(keyId)
                .isMockMode(isSimulatedMode())
                .message(isSimulatedMode()
                        ? "Test/Mock mode order created. You can verify without actual payment."
                        : "Razorpay order created successfully.")
                .build();
    }

    @Override
    @Transactional
    public SchoolSubscriptionDTO verifyPayment(Long schoolId, PaymentVerifyRequestDTO request) {
        log.info("Verifying payment for school: {} | OrderId: {} | PaymentId: {}",
                schoolId, request.getRazorpayOrderId(), request.getRazorpayPaymentId());

        boolean isMock = isSimulatedMode() || request.getRazorpayOrderId().startsWith("order_mock_");

        // 1. Signature Verification
        if (!isMock) {
            try {
                JSONObject attributes = new JSONObject();
                attributes.put("razorpay_order_id", request.getRazorpayOrderId());
                attributes.put("razorpay_payment_id", request.getRazorpayPaymentId());
                attributes.put("razorpay_signature", request.getRazorpaySignature());

                boolean isValid = Utils.verifyPaymentSignature(attributes, keySecret);
                if (!isValid) {
                    throw new RuntimeException("Invalid Razorpay payment signature! Payment verification failed.");
                }
            } catch (Exception e) {
                log.error("Signature verification error: {}", e.getMessage());
                throw new RuntimeException("Payment signature verification failed: " + e.getMessage());
            }
        } else {
            log.info("🧪 [MOCK MODE] Skipping real signature check for dummy order: {}", request.getRazorpayOrderId());
        }

        // 2. Fetch Plan
        SubscriptionPlan plan = planRepository.findByPlanType(request.getPlanType())
                .orElseThrow(() -> new RuntimeException("Plan not found: " + request.getPlanType()));

        LocalDate today = LocalDate.now();
        LocalDate endDate = (request.getBillingCycle() == BillingCycle.MONTHLY)
                ? today.plusMonths(1)
                : today.plusYears(1);

        BigDecimal paidAmount = (request.getBillingCycle() == BillingCycle.MONTHLY)
                ? plan.getMonthlyPrice()
                : plan.getYearlyPrice();

        // 3. Deactivate any existing active/trial subscriptions for this school
        subscriptionRepository.findActiveSubscription(schoolId, today)
                .ifPresent(existing -> {
                    existing.setStatus(SubscriptionStatus.CANCELLED);
                    subscriptionRepository.save(existing);
                    log.info("Deactivated previous subscription for school: {}", schoolId);
                });

        // 4. Create new ACTIVE School Subscription
        SchoolSubscription activeSub = SchoolSubscription.builder()
                .schoolId(schoolId)
                .plan(plan)
                .billingCycle(request.getBillingCycle())
                .status(SubscriptionStatus.ACTIVE)
                .startDate(today)
                .endDate(endDate)
                .isTrialPeriod(false)
                .lastPaymentDate(today)
                .nextRenewalDate(endDate)
                .build();

        SchoolSubscription savedSubscription = subscriptionRepository.save(activeSub);

        // Keep School entity in sync
        schoolRepository.findById(schoolId).ifPresent(s -> {
            s.setSubscriptionStatus("ACTIVE");
            s.setSubscriptionSource("PURCHASED");
            s.setSubscriptionStartDate(today.atStartOfDay());
            s.setSubscriptionEndDate(endDate.atTime(23, 59, 59));
            s.setSubscriptionDuration(request.getBillingCycle() == BillingCycle.MONTHLY ? "1 month" : "1 year");
            schoolRepository.save(s);
        });

        // 5. Record Payment History
        PaymentHistory paymentHistory = PaymentHistory.builder()
                .schoolId(schoolId)
                .subscription(savedSubscription)
                .amount(paidAmount)
                .currency("INR")
                .status(PaymentStatus.SUCCESS)
                .razorpayOrderId(request.getRazorpayOrderId())
                .razorpayPaymentId(request.getRazorpayPaymentId())
                .razorpaySignature(request.getRazorpaySignature())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : (isMock ? "MOCK" : "ONLINE"))
                .paidAt(LocalDateTime.now())
                .build();

        PaymentHistory savedPayment = paymentHistoryRepository.save(paymentHistory);
        log.info("Payment SUCCESS recorded: Invoice #{} for school: {} | Plan: {} | Active till: {}",
                savedPayment.getInvoiceNumber(), schoolId, plan.getPlanType(), endDate);

        // 6. Return response DTO
        long daysRemaining = ChronoUnit.DAYS.between(today, endDate);
        return SchoolSubscriptionDTO.builder()
                .id(savedSubscription.getId())
                .schoolId(schoolId)
                .plan(mapPlanToDTO(plan))
                .billingCycle(savedSubscription.getBillingCycle())
                .status(savedSubscription.getStatus())
                .startDate(savedSubscription.getStartDate())
                .endDate(savedSubscription.getEndDate())
                .daysRemaining(Math.max(0, daysRemaining))
                .isActive(true)
                .isTrialPeriod(false)
                .nextRenewalDate(savedSubscription.getNextRenewalDate())
                .createdAt(savedSubscription.getCreatedAt())
                .build();
    }

    private SubscriptionPlanDTO mapPlanToDTO(SubscriptionPlan plan) {
        BigDecimal monthlyCostForYear = plan.getMonthlyPrice().multiply(BigDecimal.valueOf(12));
        BigDecimal savings = monthlyCostForYear.subtract(plan.getYearlyPrice());

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
                .yearlySavings(savings.compareTo(BigDecimal.ZERO) > 0 ? savings : BigDecimal.ZERO)
                .build();
    }
}
