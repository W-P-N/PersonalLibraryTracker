package com.wpn.personallibrarytracker.dto;

public record ReviewResponseDTO(
        Integer reviewId,
        String content,
        Integer rating
) {
}
