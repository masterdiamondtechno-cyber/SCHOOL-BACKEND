package com.schoolmanagement.Config;

import java.io.IOException;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        LOGGER.debug("Entering JwtAuthenticationFilter for {} {}", request.getMethod(), request.getRequestURI());

        // No token — skip filter entirely (public routes)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            LOGGER.debug("No Authorization header or header does not start with Bearer. Passing through.");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);

            // Handle Mock Tokens for local development bypass
            if (token.startsWith("super-admin-mock-token-")) {
                LOGGER.info("Detected super-admin mock token. Granting ROLE_APP_ADMIN for {}", request.getRequestURI());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        "superadmin@school.com", null,
                        java.util.List.of(new SimpleGrantedAuthority("ROLE_APP_ADMIN")));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
                return;
            }
            if (token.startsWith("owner-mock-token-")) {
                LOGGER.info("Detected owner mock token. Granting ROLE_OWNER for {}", request.getRequestURI());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        "owner@school.com", null,
                        java.util.List.of(new SimpleGrantedAuthority("ROLE_OWNER")));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
                return;
            }
            if (token.startsWith("school-admin-mock-token-")) {
                LOGGER.info("Detected school-admin mock token. Granting ROLE_SCHOOL and ROLE_ADMIN for {}", request.getRequestURI());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        "school@school.com", null,
                        java.util.List.of(
                                new SimpleGrantedAuthority("ROLE_SCHOOL"),
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        ));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
                return;
            }

            LOGGER.debug("Attempting to extract username and role from JWT token");

            // ─── Extract username & role from JWT token only (NO DB query) ───
            String username = jwtUtil.extractUsername(token);
            String role     = jwtUtil.extractRole(token);

            if (username != null
                    && role != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                LOGGER.debug("Extracted username='{}', role='{}' from token", username, role);

                // ─── Validate token signature (NO DB query — pure cryptographic check) ───
                if (jwtUtil.validateToken(token, username)) {

                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    Collections.singleton(authority)
                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    LOGGER.info("JWT validated and authentication set for user='{}' with authority='ROLE_{}'", username, role);

                } else {
                    LOGGER.warn("JWT validation failed for user='{}'. Allowing security framework to handle (401)", username);
                }
            } else {
                LOGGER.debug("Username or role missing or authentication already present. username={}, role={}, existingAuth={}",
                        username, role, SecurityContextHolder.getContext().getAuthentication() != null);
            }

        } catch (JwtException | IllegalArgumentException e) {
            LOGGER.warn("Invalid JWT encountered: {}", e.getMessage());
            // Invalid token — let Spring Security handle it as 401
        }

        LOGGER.debug("Proceeding with filter chain for request {}", request.getRequestURI());
        filterChain.doFilter(request, response);
    }
}
