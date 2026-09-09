package com.schoolmanagement.app_admin_Controller;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.schoolmanagement.app_admin_DTO.SubscriptionPlanDTO;
import com.schoolmanagement.app_admin_Service.SubscriptionPlanService;

@RestController
@RequestMapping("/api/admin/subscription-plans")
public class SubscriptionPlanController {

    @Autowired
    private SubscriptionPlanService service;

    @GetMapping("/getAll")
    public ResponseEntity<Map<String, Object>> getAllActivePlans() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 200);
        response.put("status", "SUCCESS");
        response.put("message", "All active plans retrieved successfully");
        response.put("data", service.getAllActivePlans());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyPlans() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 200);
        response.put("status", "SUCCESS");
        response.put("message", "Monthly plans retrieved successfully");
        response.put("data", service.getPlansByBillingCycle("MONTHLY"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/yearly")
    public ResponseEntity<Map<String, Object>> getYearlyPlans() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", 200);
        response.put("status", "SUCCESS");
        response.put("message", "Yearly plans retrieved successfully");
        response.put("data", service.getPlansByBillingCycle("YEARLY"));
        return ResponseEntity.ok(response);
    }
}
