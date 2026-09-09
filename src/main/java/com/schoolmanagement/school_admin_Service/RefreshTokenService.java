package com.schoolmanagement.school_admin_Service;

import com.schoolmanagement.school_admin_Entity.RefreshToken;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(Long userId, String userType, String email, Long schoolId, Long academicYearId);

    RefreshToken validateRefreshToken(String token);

    RefreshToken rotateRefreshToken(RefreshToken oldToken);

    void deleteAllTokensForUser(Long userId, String userType);
}