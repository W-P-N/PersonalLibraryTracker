package com.wpn.personallibrarytracker.dto.authDTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank String userName,
        @NotBlank @Email String email,
        @NotBlank @Size(max = 128, min = 8) String password
) {
}
