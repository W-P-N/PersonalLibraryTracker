package com.wpn.personallibrarytracker.dto.noteDTOs;

import com.wpn.personallibrarytracker.utility.customValidators.NullOrNotBlank;
import jakarta.validation.constraints.Min;

public record NoteRequestDTO(
        @NullOrNotBlank
        String content,
        @Min(value = 1, message = "Must be greater than 1")
        Integer pageNumber
) {
}
