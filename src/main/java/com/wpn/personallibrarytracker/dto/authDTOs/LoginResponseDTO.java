package com.wpn.personallibrarytracker.dto.authDTOs;

public record LoginResponseDTO(
        Integer userId,
        String username,
        String email,
        String token
) {};
