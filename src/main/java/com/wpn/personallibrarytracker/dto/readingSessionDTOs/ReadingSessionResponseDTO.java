package com.wpn.personallibrarytracker.dto.readingSessionDTOs;

import java.time.LocalDateTime;

public record ReadingSessionResponseDTO(
        Integer readingSessionId,
        Integer pagesReadInSession,
        Integer endSessionPageNumber,
        LocalDateTime sessionDateTime
) {
}
