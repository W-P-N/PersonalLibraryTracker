package com.wpn.personallibrarytracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequestDTO(
        @NotBlank String userName,
        @NotBlank @Email String email
) {
}
