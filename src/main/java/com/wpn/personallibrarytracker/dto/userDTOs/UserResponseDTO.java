package com.wpn.personallibrarytracker.dto.userDTOs;

public record UserResponseDTO(
        Integer userId,
        String userName,
        String email
) {}
