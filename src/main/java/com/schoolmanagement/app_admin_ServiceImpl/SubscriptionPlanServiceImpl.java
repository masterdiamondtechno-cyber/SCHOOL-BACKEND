package com.schoolmanagement.app_admin_ServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.schoolmanagement.app_admin_DTO.SubscriptionPlanDTO;
import com.schoolmanagement.app_admin_Entity.SubscriptionPlan;
import com.schoolmanagement.app_admin_Repo.SubscriptionPlanRepository;
import com.schoolmanagement.app_admin_Service.SubscriptionPlanService;

@Service
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    @Autowired
    private SubscriptionPlanRepository repository;

    
    @Override
    public List<SubscriptionPlanDTO> getAllActivePlans() {
        return repository.findByStatus("ACTIVE").stream()
                .map(this::mapToDTO)
                .sorted((a, b) -> {
                    if (a.getPrice() == 0) return 1;
                    if (b.getPrice() == 0) return -1;
                    return a.getPrice().compareTo(b.getPrice());
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SubscriptionPlanDTO> getPlansByBillingCycle(String billingCycle) {
        return repository.findByStatus("ACTIVE").stream()
                .filter(p -> p.getBillingCycle().equalsIgnoreCase(billingCycle))
                .map(this::mapToDTO)
                .sorted((a, b) -> {
                    if (a.getPrice() == 0) return 1;
                    if (b.getPrice() == 0) return -1;
                    return a.getPrice().compareTo(b.getPrice());
                })
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionPlanDTO createPlan(SubscriptionPlanDTO dto) {
        SubscriptionPlan plan = new SubscriptionPlan();
        mapToEntity(dto, plan);
        SubscriptionPlan saved = repository.save(plan);
        return mapToDTO(saved);
    }

    @Override
    public SubscriptionPlanDTO updatePlan(Long id, SubscriptionPlanDTO dto) {
        SubscriptionPlan plan = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        mapToEntity(dto, plan);
        SubscriptionPlan saved = repository.save(plan);
        return mapToDTO(saved);
    }

    @Override
    public void deletePlan(Long id) {
        SubscriptionPlan plan = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        plan.setStatus("INACTIVE");
        repository.save(plan);
    }

    @Override
    public SubscriptionPlanDTO getPlanById(Long id) {
        SubscriptionPlan plan = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        return mapToDTO(plan);
    }

    private SubscriptionPlanDTO mapToDTO(SubscriptionPlan plan) {
        SubscriptionPlanDTO dto = new SubscriptionPlanDTO();
        dto.setPlanId(plan.getPlanId());
        dto.setPlanName(plan.getPlanName());
        dto.setPrice(plan.getPrice());
        dto.setBillingCycle(plan.getBillingCycle());
        dto.setMaxStudents(plan.getMaxStudents());
        dto.setMaxTeachers(plan.getMaxTeachers());
        dto.setStorageLimitGb(plan.getStorageLimitGb());
        dto.setFeatures(plan.getFeatures());
        if (plan.getFeatures() != null) {
            dto.setFeatureList(Arrays.asList(plan.getFeatures().split(",")));
        }
        dto.setSupportLevel(plan.getSupportLevel());
        dto.setIsMostPopular(plan.getIsMostPopular());
        dto.setStatus(plan.getStatus());
        dto.setDiscountPercentage(plan.getDiscountPercentage());
        dto.setPaymentLink(plan.getPaymentLink());
        return dto;
    }

    private void mapToEntity(SubscriptionPlanDTO dto, SubscriptionPlan plan) {
        if (dto.getPlanName() != null) plan.setPlanName(dto.getPlanName());
        if (dto.getPrice() != null) plan.setPrice(dto.getPrice());
        if (dto.getBillingCycle() != null) plan.setBillingCycle(dto.getBillingCycle());
        if (dto.getMaxStudents() != null) plan.setMaxStudents(dto.getMaxStudents());
        if (dto.getMaxTeachers() != null) plan.setMaxTeachers(dto.getMaxTeachers());
        if (dto.getStorageLimitGb() != null) plan.setStorageLimitGb(dto.getStorageLimitGb());
        
        if (dto.getFeatureList() != null) {
            plan.setFeatures(String.join(",", dto.getFeatureList()));
        } else if (dto.getFeatures() != null) {
            plan.setFeatures(dto.getFeatures());
        }
        
        if (dto.getSupportLevel() != null) plan.setSupportLevel(dto.getSupportLevel());
        if (dto.getIsMostPopular() != null) plan.setIsMostPopular(dto.getIsMostPopular());
        if (dto.getStatus() != null) {
            plan.setStatus(dto.getStatus());
        }
        if (dto.getDiscountPercentage() != null) plan.setDiscountPercentage(dto.getDiscountPercentage());
        if (dto.getPaymentLink() != null) plan.setPaymentLink(dto.getPaymentLink());
    }
}
