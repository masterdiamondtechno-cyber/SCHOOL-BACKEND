package com.schoolmanagement.school_admin_Repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.schoolmanagement.school_admin_Entity.RefreshToken;

import jakarta.transaction.Transactional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    // Delete all refresh tokens for a user (used on logout)
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken r WHERE r.userId = :userId AND r.userType = :userType")
    void deleteByUserIdAndUserType(Long userId, String userType);

    // Revoke all tokens for a user (used on theft detection)
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken r SET r.isRevoked = true WHERE r.userId = :userId AND r.userType = :userType")
    void revokeAllByUserIdAndUserType(Long userId, String userType);

    void deleteBySchoolId(Long schoolId);
}