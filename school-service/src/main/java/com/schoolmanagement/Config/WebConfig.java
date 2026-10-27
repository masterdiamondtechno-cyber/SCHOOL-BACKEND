package com.schoolmanagement.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AcademicYearInterceptor academicYearInterceptor;
    private final SubscriptionInterceptor subscriptionInterceptor;

    public WebConfig(AcademicYearInterceptor academicYearInterceptor,
                     SubscriptionInterceptor subscriptionInterceptor) {
        this.academicYearInterceptor = academicYearInterceptor;
        this.subscriptionInterceptor = subscriptionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // Academic Year interceptor — sab APIs pe
        registry.addInterceptor(academicYearInterceptor)
                .addPathPatterns("/**");

        // Subscription interceptor — sab APIs pe EXCEPT public ones
        registry.addInterceptor(subscriptionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // Auth — login band hoga toh renew bhi nahi hoga!
                        "/api/auth/**",
                        "/api/auth/admin/**",

                        // School registration & OTP
                        "/api/AddSchool/create",
                        "/api/AddSchool/sendOtp",
                        "/api/AddSchool/verifyOtp",
                        "/api/AddSchool/updatePassword",
                        "/student/changePassword",

                        // Subscription APIs — plans dekhna, payment karna
                        "/api/subscription/**",

                        // System
                        "/api/status",
                        "/api/public/**",

                        // Super Admin & Owner — unka subscription nahi hota
                        "/api/app_admin/**",

                        // Swagger & Actuator
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/actuator/**"
                );
    }
}
