package com.schoolmanagement.client;

import com.schoolmanagement.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "subscription-service")
public interface SubscriptionClient {

    @GetMapping("/api/subscription/check/{schoolId}")
    ApiResponse<Boolean> checkSubscription(@PathVariable("schoolId") Long schoolId);
}
