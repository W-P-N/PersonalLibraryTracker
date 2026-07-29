package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.authDTOs.AuthResponseDTO;
import com.wpn.personallibrarytracker.dto.authDTOs.LoginRequestDTO;
import com.wpn.personallibrarytracker.dto.authDTOs.RefreshTokenRequestDTO;
import com.wpn.personallibrarytracker.dto.authDTOs.RegisterRequestDTO;
import com.wpn.personallibrarytracker.dto.userDTOs.UserResponseDTO;

public interface AuthService {
    UserResponseDTO registerUser(RegisterRequestDTO registerRequestDTO);
    AuthResponseDTO loginUser(LoginRequestDTO loginRequestDTO);
    AuthResponseDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequestDTO);
}
