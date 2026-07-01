package com.wpn.personallibrarytracker.dto;

import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

public record NoteResponseDTO(
        Integer noteId,
        String content,
        LocalDateTime createdAt,
        @Min(1)
        Integer pageNumber
) {
}
