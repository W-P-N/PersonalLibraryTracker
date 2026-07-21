package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.userDTOs.UserCreateRequestDTO;
import com.wpn.personallibrarytracker.dto.userDTOs.UserResponseDTO;
import com.wpn.personallibrarytracker.dto.userDTOs.UserUpdateRequestDTO;
import com.wpn.personallibrarytracker.exceptions.UserAlreadyExistsException;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUser(
            @PathVariable Integer userId
    ) throws UserNotFoundException {
        UserResponseDTO userResponseDTO = userService.getUser(userId);
        return new ResponseEntity<>(userResponseDTO, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid
            @RequestBody
            UserCreateRequestDTO userCreateRequestDTO
    ) throws UserAlreadyExistsException {
        UserResponseDTO userResponseDTO = userService.registerUser(userCreateRequestDTO);
        return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable
            Integer userId,
            @Valid
            @RequestBody
            UserUpdateRequestDTO userUpdateRequestDTO
    ) throws UserNotFoundException {
        UserResponseDTO userResponseDTO = userService.updateUser(userId, userUpdateRequestDTO);
        return new ResponseEntity<>(userResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable
            Integer userId
    ) throws UserNotFoundException {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
