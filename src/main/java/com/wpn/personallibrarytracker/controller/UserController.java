package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.userDTOs.UserResponseDTO;
import com.wpn.personallibrarytracker.dto.userDTOs.UserUpdateRequestDTO;
import com.wpn.personallibrarytracker.exceptions.ResourceNotFoundException;
import com.wpn.personallibrarytracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/me")
@Validated
public class UserController {
    private final UserService userService;

    public UserController(
            UserService userService
    ) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserResponseDTO> getUser(
            @AuthenticationPrincipal Integer userId
    ) throws ResourceNotFoundException {
        UserResponseDTO userResponseDTO = userService.getUser(userId);
        return new ResponseEntity<>(userResponseDTO, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<UserResponseDTO> updateUser(
            @AuthenticationPrincipal
            Integer userId,
            @Valid
            @RequestBody
            UserUpdateRequestDTO userUpdateRequestDTO
    ) throws ResourceNotFoundException {
        UserResponseDTO userResponseDTO = userService.updateUser(userId, userUpdateRequestDTO);
        return new ResponseEntity<>(userResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(
            @AuthenticationPrincipal
            Integer userId
    ) throws ResourceNotFoundException {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
