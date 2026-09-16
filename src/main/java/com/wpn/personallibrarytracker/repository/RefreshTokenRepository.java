package com.wpn.personallibrarytracker.repository;

import com.wpn.personallibrarytracker.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    void deleteByExpiryDateBefore(LocalDateTime dateTime);
}
