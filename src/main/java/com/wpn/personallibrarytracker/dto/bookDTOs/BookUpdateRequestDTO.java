package com.wpn.personallibrarytracker.dto.bookDTOs;

import jakarta.validation.constraints.Min;

public record BookUpdateRequestDTO(
        String title,
        String author,
        @Min(1) Integer totalPages,
        String isbn,
        String coverUrl
) {
}
