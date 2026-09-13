package com.wpn.personallibrarytracker.dto.bookDTOs;

import com.wpn.personallibrarytracker.dto.noteDTOs.NoteResponseDTO;
import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewResponseDTO;

import java.util.List;

public record BookDetailsResponseDTO(
        Integer bookId,
        String title,
        String author,
        Integer totalPages,
        String isbn,
        String coverUrl,
        ReviewResponseDTO review
) {
}
