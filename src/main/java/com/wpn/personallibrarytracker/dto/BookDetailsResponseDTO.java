package com.wpn.personallibrarytracker.dto;

import com.wpn.personallibrarytracker.entity.Note;
import com.wpn.personallibrarytracker.entity.ReadingSession;
import com.wpn.personallibrarytracker.entity.Review;

import java.util.List;

public record BookDetailsResponseDTO(
        Integer bookId,
        String title,
        String author,
        Integer totalPages,
        String isbn,
        String coverUrl,
        List<ReadingSessionResponseDTO> readingSessionList,
        List<NoteResponseDTO> notes,
        ReviewResponseDTO review
) {
}
