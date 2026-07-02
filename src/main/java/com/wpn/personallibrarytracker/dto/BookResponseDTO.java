package com.wpn.personallibrarytracker.dto;

import com.wpn.personallibrarytracker.entity.ReadingSession;

import java.util.List;

public record BookResponseDTO (
        Integer bookId,
        String title,
        String author,
        String isbn,
        String coverUrl,
        Integer totalPages
) {
}
