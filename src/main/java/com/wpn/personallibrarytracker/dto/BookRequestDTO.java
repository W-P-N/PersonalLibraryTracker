package com.wpn.personallibrarytracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookRequestDTO(
        @NotBlank
        String title,
        @NotBlank
        String author,
        @NotBlank
        @NotNull
        String isbn,
        String coverUrl,
        @NotNull
        Integer totalPages
) {
}
