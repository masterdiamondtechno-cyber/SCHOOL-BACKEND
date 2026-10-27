package com.schoolmanagement.Config;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private static final String SECRET_KEY  = "mysecretkeymysecretkeymysecretkey12";
    private static final long   ACCESS_EXPIRY = 1000L * 60 * 60;       // 1 hour

    private Key signingKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // ── Generate access token with claims ────────────────────
    public String generateToken(String username, Map<String, Object> claims) {
        long expiry = ACCESS_EXPIRY;
        if ("APP_ADMIN".equalsIgnoreCase(String.valueOf(claims.get("role")))) {
            expiry = 1000L * 60 * 60 * 4; // 4 hours for Admin
        }
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiry))
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    public String  extractUsername(String token)          { return extractAllClaims(token).getSubject(); }
    public Object  extractClaim(String token, String key) { return extractAllClaims(token).get(key); }

    private boolean isExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public boolean validateToken(String token, String username) {
        String subject = extractUsername(token);
        // subject may be null if JWT was created without setSubject() — fall back to uid claim
        if (subject == null) {
            // Token is valid if role claim is present and token is not expired
            Object roleClaim = extractClaim(token, "role");
            return roleClaim != null && !isExpired(token);
        }
        return subject.equals(username) && !isExpired(token);
    }
}