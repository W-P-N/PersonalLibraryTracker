package com.wpn.personallibrarytracker.dto.reviewDTOs;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ReviewUpdateRequestDTO(
        String content,
        @Min(1)
        @Max(5)
        Integer rating
) {
}
