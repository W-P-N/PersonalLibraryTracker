package com.wpn.personallibrarytracker.dto.readingSessionDTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReadingSessionRequestDTO(
        @NotNull @Min(1) Integer endSessionPageNumber
) {
}
