package com.wpn.personallibrarytracker.dto.bookDTOs;

public record BookResponseDTO (
        Integer bookId,
        String title,
        String author,
        String isbn,
        String coverUrl,
        Integer totalPages
) {
}
