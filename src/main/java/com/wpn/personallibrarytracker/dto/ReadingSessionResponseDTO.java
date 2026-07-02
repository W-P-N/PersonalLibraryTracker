package com.wpn.personallibrarytracker.dto;

import java.time.LocalDateTime;

public record ReadingSessionResponseDTO(
        Integer readingSessionId,
        Integer pagesReadInSession,
        Integer endSessionPageNumber,
        LocalDateTime sessionDateTime
) {
}
