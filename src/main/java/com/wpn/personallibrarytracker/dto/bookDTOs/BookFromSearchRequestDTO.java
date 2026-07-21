package com.wpn.personallibrarytracker.dto.bookDTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookFromSearchRequestDTO(
        @NotBlank
        String title,
        @NotBlank
        String author,
        @NotNull
        Integer totalPages,
        String isbn,
        String coverUrl
) {
}
