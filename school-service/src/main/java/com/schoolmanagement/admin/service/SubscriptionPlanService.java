package com.schoolmanagement.admin.service;

import java.util.List;
import com.schoolmanagement.admin.dto.SubscriptionPlanDTO;

public interface SubscriptionPlanService {
    List<SubscriptionPlanDTO> getAllActivePlans();
    List<SubscriptionPlanDTO> getPlansByBillingCycle(String billingCycle);
    SubscriptionPlanDTO createPlan(SubscriptionPlanDTO dto);
    SubscriptionPlanDTO updatePlan(Long id, SubscriptionPlanDTO dto);
    void deletePlan(Long id);
    SubscriptionPlanDTO getPlanById(Long id);
}
