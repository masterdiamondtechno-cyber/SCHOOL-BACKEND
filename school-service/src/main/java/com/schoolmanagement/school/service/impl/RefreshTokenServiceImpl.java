package com.schoolmanagement.school.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.schoolmanagement.school.entity.RefreshToken;
import com.schoolmanagement.school.repository.RefreshTokenRepository;
import com.schoolmanagement.school.service.RefreshTokenService;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final int REFRESH_TOKEN_EXPIRY_DAYS = 7;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    // ── Create a new refresh token ───────────────────────────
    public RefreshToken createRefreshToken(Long userId, String userType, String email, Long schoolId,
            Long academicYearId) {

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUserId(userId);
        refreshToken.setUserType(userType);
        refreshToken.setEmail(email);
        refreshToken.setSchoolId(schoolId);
        refreshToken.setAcademicYearId(academicYearId);
        refreshToken.setExpiry(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRY_DAYS));
        refreshToken.setIsRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    // ── Validate refresh token ───────────────────────────────
    public RefreshToken validateRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // Check if revoked — possible theft detected
        if (refreshToken.getIsRevoked()) {
            // Revoke ALL tokens for this user — security wipe
            refreshTokenRepository.revokeAllByUserIdAndUserType(
                    refreshToken.getUserId(),
                    refreshToken.getUserType());
            throw new RuntimeException("Refresh token has been revoked. Please login again");
        }

        // Check if expired
        if (refreshToken.getExpiry().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired. Please login again");
        }

        return refreshToken;
    }

    // ── Rotate — revoke old, return new ─────────────────────
    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {

        // Revoke old token
        oldToken.setIsRevoked(true);
        refreshTokenRepository.save(oldToken);

        // Create new token
        return createRefreshToken(
                oldToken.getUserId(),
                oldToken.getUserType(),
                oldToken.getEmail(),
                oldToken.getSchoolId(),
                oldToken.getAcademicYearId());
    }

    // ── Delete all tokens for user (logout) ──────────────────
    public void deleteAllTokensForUser(Long userId, String userType) {
        refreshTokenRepository.deleteByUserIdAndUserType(userId, userType);
    }
}
