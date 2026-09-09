package com.schoolmanagement.Config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AcademicYearInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String headerVal = request.getHeader("x-academic-year-id");
        if (headerVal != null && !headerVal.trim().isEmpty()) {
            try {
                Long academicYearId = Long.parseLong(headerVal.trim());
                AcademicYearContextHolder.setCurrentAcademicYearId(academicYearId);
            } catch (NumberFormatException e) {
                // Ignore parsing errors, let thread continue normally
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        AcademicYearContextHolder.clear();
    }
}
