package com.wpn.personallibrarytracker.dto;

import lombok.Data;

public record UserResponseDTO(
        Integer userId,
        String userName,
        String email
) {}
