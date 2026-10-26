package com.schoolmanagement.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"com.schoolmanagement.auth", "com.schoolmanagement.Config", "com.schoolmanagement.school", "com.schoolmanagement.admin", "com.schoolmanagement.owner", "com.schoolmanagement.common"})
@EntityScan(basePackages = "com.schoolmanagement")
@EnableJpaRepositories(basePackages = "com.schoolmanagement")
@EnableDiscoveryClient
@EnableAsync
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
        System.out.println("==================================================");
        System.out.println("   Auth Service Started on Port 8081              ");
        System.out.println("   Registered with Eureka as AUTH-SERVICE         ");
        System.out.println("==================================================");
    }
}
