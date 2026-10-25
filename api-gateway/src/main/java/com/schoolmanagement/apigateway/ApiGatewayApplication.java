package com.schoolmanagement.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
        System.out.println("==================================================");
        System.out.println("   API Gateway Started on Port 8080               ");
        System.out.println("   Central Routing Active via Eureka              ");
        System.out.println("==================================================");
    }
}
