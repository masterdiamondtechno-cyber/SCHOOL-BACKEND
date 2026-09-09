package com.schoolmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.schoolmanagement.app_admin_Service.SubscriptionPlanService;
import com.schoolmanagement.app_admin_Repo.SubscriptionPlanRepository;
import com.schoolmanagement.app_admin_Entity.SubscriptionPlan;
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

