package com.schoolmanagement.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.subscription.service.SubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionInterceptor implements HandlerInterceptor {

    private final SubscriptionService subscriptionService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Step 1: JWT token nikalo header se
        String authHeader = request.getHeader("Authorization");

        // Token nahi hai → Security filter handle karega, hum skip karte hain
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return true;
        }

        String token = authHeader.substring(7);

        // Step 2: Role nikalo token se
        String role;
        Long schoolId;
        try {
            role    = jwtUtil.extractRole(token);
            Object schoolIdClaim = jwtUtil.extractClaim(token, "sid");  // "sid" = schoolId in JWT
            schoolId = schoolIdClaim != null ? Long.parseLong(schoolIdClaim.toString()) : null;
        } catch (Exception e) {
            // Token invalid hai → Security filter handle karega
            return true;
        }

        // Step 3: Sirf school-level users ke liye check karo
        // APP_ADMIN aur OWNER ka subscription check nahi hoga — wo master hain!
        if (role == null || role.equals("APP_ADMIN") || role.equals("OWNER")) {
            return true;
        }

        // Step 4: School ID nahi mili token mein → skip
        if (schoolId == null) {
            return true;
        }

        // Step 5: Subscription check karo!
        boolean isActive = subscriptionService.isSubscriptionActive(schoolId);

        if (isActive) {
            // ✅ Subscription valid hai → API aage chale
            return true;
        } else {
            // ❌ Subscription expire / nahi hai → Block karo!
            log.warn("Subscription check FAILED for school: {} | URL: {}", schoolId, request.getRequestURI());

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);  // 403
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("success", false);
            errorBody.put("status", 403);
            errorBody.put("message", "Your school subscription has expired or is inactive. " +
                    "Please renew your plan at masterdiamond.in/plans");
            errorBody.put("action", "RENEW_SUBSCRIPTION");

            response.getWriter().write(objectMapper.writeValueAsString(errorBody));
            return false;  // ← API aage NAHI chalega
        }
    }
}
