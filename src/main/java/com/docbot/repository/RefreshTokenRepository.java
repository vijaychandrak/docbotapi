package com.docbot.repository;

import com.docbot.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for RefreshToken entity.
 * Provides database access for JWT refresh token management.
 * Supports token validation, revocation, and cleanup.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUserId(UUID userId);

    List<RefreshToken> findByUserIdAndRevokedAtIsNull(UUID userId);

    long countByUserId(UUID userId);

    @Transactional
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = CURRENT_TIMESTAMP WHERE rt.user.id = :userId")
    void revokeAllUserTokens(@Param("userId") UUID userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < CURRENT_TIMESTAMP")
    int deleteExpiredTokens();

    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :expiryDate")
    int deleteTokensOlderThan(@Param("expiryDate") LocalDateTime expiryDate);

    long countByUserIdAndRevokedAtIsNotNull(UUID userId);
}
