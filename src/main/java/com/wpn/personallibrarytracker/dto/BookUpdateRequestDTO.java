package com.wpn.personallibrarytracker.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BookUpdateRequestDTO(
        String title,
        String author,
        @Min(1) Integer totalPages,
        String isbn,
        String coverUrl
) {
}
