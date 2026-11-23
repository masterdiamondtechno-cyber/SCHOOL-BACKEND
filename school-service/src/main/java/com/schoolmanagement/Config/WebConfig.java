package com.schoolmanagement.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private final AcademicYearInterceptor academicYearInterceptor;
    private final SubscriptionInterceptor subscriptionInterceptor;

    public WebConfig(AcademicYearInterceptor academicYearInterceptor,
                     SubscriptionInterceptor subscriptionInterceptor) {
        this.academicYearInterceptor = academicYearInterceptor;
        this.subscriptionInterceptor = subscriptionInterceptor;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String uploadLocation = uploadPath.toUri().toString();
        if (!uploadLocation.endsWith("/")) {
            uploadLocation += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // Academic Year interceptor — sab APIs pe EXCEPT static uploads
        registry.addInterceptor(academicYearInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/uploads/**");

        // Subscription interceptor — sab APIs pe EXCEPT public ones and static uploads
        registry.addInterceptor(subscriptionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // Uploads / Static files (logos, study material, etc.)
                        "/uploads/**",

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
