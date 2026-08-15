package com.wpn.personallibrarytracker.dto.readingSessionDTOs;

import java.time.LocalDateTime;

public record ReadingSessionDetailsResponseDTO(
        Integer readingSessionId,
        Integer pagesReadInSession,
        Integer endSessionPageNumber,
        LocalDateTime sessionDateTime
) {
}
