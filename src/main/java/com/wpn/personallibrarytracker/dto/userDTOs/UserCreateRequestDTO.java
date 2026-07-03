package com.wpn.personallibrarytracker.dto.userDTOs;

import jakarta.validation.constraints.*;

public record UserCreateRequestDTO(
        @NotBlank String userName,
        @NotBlank @Email String email,
        @NotBlank @Size(max = 128, min = 8) String password
) {}
