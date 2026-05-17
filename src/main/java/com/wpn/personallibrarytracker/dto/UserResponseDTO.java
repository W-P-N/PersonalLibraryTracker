package com.wpn.personallibrarytracker.dto;

public record UserResponseDTO(
        Integer userId,
        String userName,
        String email
) {}
