package com.schoolmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.schoolmanagement.admin.service.SubscriptionPlanService;
import com.schoolmanagement.admin.repository.SubscriptionPlanRepository;
import com.schoolmanagement.admin.entity.SubscriptionPlan;
import java.util.List;

@SpringBootTest
class ApplicationTests {

	@Autowired
	private SubscriptionPlanRepository repository;

	@Autowired
	private SubscriptionPlanService service;

	@Test
	void contextLoads() {
		try {
			System.out.println("=== DIAGNOSTIC: CALLING SERVICE.GETALLACTIVEPLANS ===");
			service.getAllActivePlans();
			System.out.println("Service call succeeded!");
		} catch (Throwable t) {
			System.out.println("Service call failed with exception:");
			t.printStackTrace();
		}
	}

}

