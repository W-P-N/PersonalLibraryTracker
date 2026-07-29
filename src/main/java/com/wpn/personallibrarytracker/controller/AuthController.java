package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.authDTOs.AuthResponseDTO;
import com.wpn.personallibrarytracker.dto.authDTOs.LoginRequestDTO;
import com.wpn.personallibrarytracker.dto.authDTOs.RefreshTokenRequestDTO;
import com.wpn.personallibrarytracker.dto.authDTOs.RegisterRequestDTO;
import com.wpn.personallibrarytracker.dto.userDTOs.UserResponseDTO;
import com.wpn.personallibrarytracker.exceptions.UserAlreadyExistsException;
import com.wpn.personallibrarytracker.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid
            @RequestBody
            RegisterRequestDTO registerRequestDTO
    ) throws UserAlreadyExistsException {
        UserResponseDTO userResponseDTO = authService.registerUser(registerRequestDTO);
        return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> loginUser(
            @Valid
            @RequestBody
            LoginRequestDTO loginRequestDTO
    ) {
        return new ResponseEntity<>(
            authService.loginUser(loginRequestDTO),
                HttpStatus.OK
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refreshToken(
            @Valid
            @RequestBody
            RefreshTokenRequestDTO refreshTokenRequestDTO
    ) {
        return new ResponseEntity<>(
                authService.refreshToken(refreshTokenRequestDTO),
                HttpStatus.OK
        );
    }
}
