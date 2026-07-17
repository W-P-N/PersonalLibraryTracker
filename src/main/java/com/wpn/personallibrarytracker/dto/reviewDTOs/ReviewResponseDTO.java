package com.wpn.personallibrarytracker.dto.reviewDTOs;

import java.time.LocalDateTime;

public record ReviewResponseDTO(
        String content,
        Integer rating,
        LocalDateTime createdAt
) {
}
