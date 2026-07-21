package com.wpn.personallibrarytracker.dto.noteDTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record NoteUpdateRequestDTO(
        String content,
        @Min(value = 1, message = "Must be greater than 1")
        Integer pageNumber
) {}
