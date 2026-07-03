package com.wpn.personallibrarytracker.dto.reviewDTOs;

public record ReviewResponseDTO(
        Integer reviewId,
        String content,
        Integer rating
) {
}
