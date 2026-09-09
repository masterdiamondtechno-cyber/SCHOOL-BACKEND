package com.schoolmanagement.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AcademicYearInterceptor academicYearInterceptor;

    public WebConfig(AcademicYearInterceptor academicYearInterceptor) {
        this.academicYearInterceptor = academicYearInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(academicYearInterceptor).addPathPatterns("/**");
    }
}
