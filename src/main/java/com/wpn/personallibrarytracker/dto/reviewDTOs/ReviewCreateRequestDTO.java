package com.wpn.personallibrarytracker.dto.reviewDTOs;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequestDTO(
        @NotBlank
        String constraint,
        @NotNull
        @Min(1)
        @Max(5)
        Integer rating
) {
}
