package com.wpn.personallibrarytracker.dto.authDTOs;

public record AuthResponseDTO(
        Integer userId,
        String username,
        String email,
        String token,
        String refreshToken
) {
}
