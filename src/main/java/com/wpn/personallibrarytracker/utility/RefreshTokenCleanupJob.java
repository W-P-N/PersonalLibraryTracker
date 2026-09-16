package com.wpn.personallibrarytracker.utility;

import com.wpn.personallibrarytracker.repository.RefreshTokenRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class RefreshTokenCleanupJob {
    private static final Logger LOGGER = LogManager.getLogger(RefreshTokenCleanupJob.class);
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenCleanupJob(
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void purgeExpiredRefreshTokens() {
        LOGGER.info("Starting scheduled cleanup of expired refresh tokens");
        refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
        LOGGER.info("Completed scheduled cleanup of expired refresh tokens");
    }
}
