package com.wpn.personallibrarytracker.dto;

public record BookSearchResponseDTO(
        String title,
        String description,
        String author,
        String isbn,
        String coverUrl,
        Integer totalPages
) {
}
