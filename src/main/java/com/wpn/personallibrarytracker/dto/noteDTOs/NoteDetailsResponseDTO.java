package com.wpn.personallibrarytracker.dto.noteDTOs;

import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

public record NoteDetailsResponseDTO(
        Integer noteId,
        String content,
        LocalDateTime createdAt,
        Integer pageNumber
) {
}
