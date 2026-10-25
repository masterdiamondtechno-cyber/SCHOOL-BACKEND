package com.schoolmanagement.subscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.schoolmanagement.subscription", "com.schoolmanagement.Config", "com.schoolmanagement.common"})
@EntityScan(basePackages = {"com.schoolmanagement.subscription.entity", "com.schoolmanagement.school.entity"})
@EnableJpaRepositories(basePackages = {"com.schoolmanagement.subscription.repository", "com.schoolmanagement.school.repository"})
@EnableDiscoveryClient
@EnableScheduling
@EnableAsync
public class SubscriptionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubscriptionServiceApplication.class, args);
        System.out.println("==================================================");
        System.out.println("   Subscription & Billing Service on Port 8082    ");
        System.out.println("   Registered with Eureka as SUBSCRIPTION-SERVICE ");
        System.out.println("==================================================");
    }
}
