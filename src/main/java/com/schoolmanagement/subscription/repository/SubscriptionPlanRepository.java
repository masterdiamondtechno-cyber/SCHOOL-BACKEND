package com.schoolmanagement.subscription.repository;

import com.schoolmanagement.subscription.entity.SubscriptionPlan;
import com.schoolmanagement.subscription.entity.SubscriptionPlan.PlanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("saasSubscriptionPlanRepository")
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    // Plan type se dhundho (BASIC, PRO, ENTERPRISE)
    Optional<SubscriptionPlan> findByPlanType(PlanType planType);

    // Sirf active plans dikhao (jo available hain)
    List<SubscriptionPlan> findByIsActiveTrue();
}
