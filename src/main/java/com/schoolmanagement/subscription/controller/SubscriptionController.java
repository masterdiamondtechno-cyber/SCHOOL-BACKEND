package com.schoolmanagement.subscription.controller;

import com.schoolmanagement.common.ApiResponse;
import com.schoolmanagement.subscription.dto.*;
import com.schoolmanagement.subscription.entity.SubscriptionPlan.PlanType;
import com.schoolmanagement.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "SaaS Subscription Management")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // ─── Public APIs (No Auth needed) ───────────────────

    @GetMapping("/plans")
    @Operation(summary = "Get all available subscription plans")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanDTO>>> getAllPlans() {
        List<SubscriptionPlanDTO> plans = subscriptionService.getAllPlans();
        return ResponseEntity.ok(ApiResponse.success("Plans fetched successfully", plans));
    }

    @GetMapping("/plans/{planType}")
    @Operation(summary = "Get specific plan details")
    public ResponseEntity<ApiResponse<SubscriptionPlanDTO>> getPlanByType(
            @PathVariable PlanType planType) {
        SubscriptionPlanDTO plan = subscriptionService.getPlanByType(planType);
        return ResponseEntity.ok(ApiResponse.success("Plan fetched successfully", plan));
    }

    // ─── School APIs (School Admin ke liye) ─────────────

    @PostMapping("/trial/{schoolId}")
    @Operation(summary = "Start 14-day free trial for a school")
    @PreAuthorize("hasRole('SCHOOL') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SchoolSubscriptionDTO>> startTrial(
            @PathVariable Long schoolId) {
        SchoolSubscriptionDTO result = subscriptionService.startFreeTrial(schoolId);
        return ResponseEntity.ok(ApiResponse.success("Free trial started successfully! Enjoy 14 days free.", result));
    }

    @PostMapping("/subscribe/{schoolId}")
    @Operation(summary = "Subscribe school to a plan")
    @PreAuthorize("hasRole('SCHOOL') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SchoolSubscriptionDTO>> subscribe(
            @PathVariable Long schoolId,
            @Valid @RequestBody SubscribeRequestDTO request) {
        SchoolSubscriptionDTO result = subscriptionService.subscribe(schoolId, request);
        return ResponseEntity.ok(ApiResponse.success("Subscription activated successfully!", result));
    }

    @GetMapping("/current/{schoolId}")
    @Operation(summary = "Get current subscription of a school")
    @PreAuthorize("hasRole('SCHOOL') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SchoolSubscriptionDTO>> getCurrentSubscription(
            @PathVariable Long schoolId) {
        SchoolSubscriptionDTO result = subscriptionService.getCurrentSubscription(schoolId);
        return ResponseEntity.ok(ApiResponse.success("Subscription fetched", result));
    }

    @DeleteMapping("/cancel/{schoolId}")
    @Operation(summary = "Cancel school subscription")
    @PreAuthorize("hasRole('SCHOOL') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SchoolSubscriptionDTO>> cancelSubscription(
            @PathVariable Long schoolId,
            @RequestParam(defaultValue = "Cancelled by user") String reason) {
        SchoolSubscriptionDTO result = subscriptionService.cancelSubscription(schoolId, reason);
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled", result));
    }

    @GetMapping("/check/{schoolId}")
    @Operation(summary = "Check if school subscription is active")
    @PreAuthorize("hasRole('SCHOOL') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> checkSubscription(
            @PathVariable Long schoolId) {
        boolean isActive = subscriptionService.isSubscriptionActive(schoolId);
        String message = isActive ? "Subscription is active" : "Subscription is not active";
        return ResponseEntity.ok(ApiResponse.success(message, isActive));
    }

    @GetMapping("/payments/{schoolId}")
    @Operation(summary = "Get payment history of a school")
    @PreAuthorize("hasRole('SCHOOL') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentHistoryDTO>>> getPaymentHistory(
            @PathVariable Long schoolId) {
        List<PaymentHistoryDTO> history = subscriptionService.getPaymentHistory(schoolId);
        return ResponseEntity.ok(ApiResponse.success("Payment history fetched", history));
    }
}
