package com.schoolmanagement.app_admin_Controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.app_admin_Entity.SubscriptionAssignmentHistory;
import com.schoolmanagement.app_admin_Entity.SubscriptionPlan;
import com.schoolmanagement.app_admin_Repo.SubscriptionAssignmentHistoryRepository;
import com.schoolmanagement.app_admin_Repo.SubscriptionPlanRepository;
import com.schoolmanagement.school_admin_Entity.School;
import com.schoolmanagement.school_admin_Repo.SchoolRepository;

@RestController
@RequestMapping("/api/public/razorpay")
public class RazorpayWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(RazorpayWebhookController.class);

    @Value("${razorpay.webhook.secret:razorpay_default_secret_key}")
    private String webhookSecret;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private SubscriptionAssignmentHistoryRepository historyRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            logger.info("Received Razorpay webhook request");

            // 1. Validate Webhook Signature if signature header is present and secret is configured
            if (signature != null && webhookSecret != null && !webhookSecret.equals("razorpay_default_secret_key")) {
                boolean isValid = verifySignature(payload, signature, webhookSecret);
                if (!isValid) {
                    logger.warn("Razorpay webhook signature verification failed");
                    response.put("code", 401);
                    response.put("status", "UNAUTHORIZED");
                    response.put("message", "Invalid Razorpay signature");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
            }

            // 2. Parse JSON Payload using Jackson
            JsonNode root = objectMapper.readTree(payload);
            String event = root.path("event").asText("");

            logger.info("Razorpay webhook event: {}", event);

            if ("payment.captured".equalsIgnoreCase(event) || "order.paid".equalsIgnoreCase(event)) {
                JsonNode paymentEntity = root.path("payload").path("payment").path("entity");
                String paymentId = paymentEntity.path("id").asText("");
                JsonNode notesNode = paymentEntity.path("notes");

                Long schoolId = null;
                Long planId = null;

                if (notesNode != null && !notesNode.isMissingNode()) {
                    if (notesNode.has("school_id")) {
                        schoolId = notesNode.get("school_id").asLong();
                    }
                    if (notesNode.has("plan_id")) {
                        planId = notesNode.get("plan_id").asLong();
                    }
                }

                logger.info("Processing webhook for schoolId: {}, planId: {}, paymentId: {}", schoolId, planId, paymentId);

                if (schoolId != null && planId != null) {
                    processSuccessfulSubscription(schoolId, planId, paymentId);
                }
            }

            response.put("code", 200);
            response.put("status", "SUCCESS");
            response.put("message", "Webhook processed successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error processing Razorpay webhook", e);
            response.put("code", 500);
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private void processSuccessfulSubscription(Long schoolId, Long planId, String paymentId) {
        School school = schoolRepository.findById(schoolId).orElse(null);
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId).orElse(null);

        if (school != null && plan != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endDate = "YEARLY".equalsIgnoreCase(plan.getBillingCycle())
                    ? now.plusYears(1)
                    : now.plusMonths(1);

            // Update School Active Subscription Details
            school.setSubscriptionPlan(plan);
            school.setSubscriptionStatus("ACTIVE");
            school.setSubscriptionSource("RAZORPAY");
            school.setSubscriptionStartDate(now);
            school.setSubscriptionEndDate(endDate);
            school.setSubscriptionDuration(plan.getBillingCycle());
            school.setManualAssignmentDate(now);

            schoolRepository.save(school);
            logger.info("Successfully updated subscription for school ID: {} to plan: {}", schoolId, plan.getPlanName());

            // Save Audit Log to SubscriptionAssignmentHistory
            SubscriptionAssignmentHistory history = new SubscriptionAssignmentHistory();
            history.setSchoolId(schoolId);
            history.setPlanId(planId);
            history.setPlanName(plan.getPlanName());
            history.setSubscriptionSource("RAZORPAY (" + paymentId + ")");
            history.setAssignmentDate(now);
            history.setStartDate(now);
            history.setEndDate(endDate);
            history.setDuration(plan.getBillingCycle());
            history.setReason("Payment captured via Razorpay. Payment ID: " + paymentId);

            historyRepository.save(history);
            logger.info("Saved subscription history record for school ID: {}", schoolId);
        } else {
            logger.warn("School or Plan not found for schoolId: {}, planId: {}", schoolId, planId);
        }
    }

    private boolean verifySignature(String payload, String signature, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().equalsIgnoreCase(signature);
        } catch (Exception e) {
            logger.error("Error computing HmacSHA256 signature", e);
            return false;
        }
    }
}
