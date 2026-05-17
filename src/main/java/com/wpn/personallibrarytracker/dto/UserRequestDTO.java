package com.wpn.personallibrarytracker.dto;

import jakarta.validation.constraints.*;

public record UserRequestDTO(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank @Size(max = 128, min = 8) String password
) {}
