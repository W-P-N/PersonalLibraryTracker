package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.authDTOs.LoginRequestDTO;
import com.wpn.personallibrarytracker.dto.authDTOs.LoginResponseDTO;
import com.wpn.personallibrarytracker.dto.authDTOs.RegisterRequestDTO;
import com.wpn.personallibrarytracker.dto.userDTOs.UserCreateRequestDTO;
import com.wpn.personallibrarytracker.dto.userDTOs.UserResponseDTO;

public interface AuthService {
    UserResponseDTO registerUser(RegisterRequestDTO registerRequestDTO);
    LoginResponseDTO loginUser(LoginRequestDTO loginRequestDTO);
}
