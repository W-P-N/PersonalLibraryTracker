package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.UserCreateRequestDTO;
import com.wpn.personallibrarytracker.dto.UserResponseDTO;
import com.wpn.personallibrarytracker.dto.UserUpdateRequestDTO;
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
    public ResponseEntity<UserResponseDTO> getUserDetails(@PathVariable Integer userId) throws UserNotFoundException {
        UserResponseDTO userResponseDTO = userService.getUserById(userId);
        return new ResponseEntity<>(userResponseDTO, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> postUserDetails(
            @Valid
            @RequestBody
            UserCreateRequestDTO userCreateRequestDTO
    ) throws UserAlreadyExistsException {
        UserResponseDTO userResponseDTO = userService.postUserDetails(userCreateRequestDTO);
        return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> putUserDetails(
            @PathVariable
            Integer userId,
            @Valid
            @RequestBody
            UserUpdateRequestDTO userUpdateRequestDTO
    ) throws UserNotFoundException {
        UserResponseDTO userResponseDTO = userService.putUserDetails(userId, userUpdateRequestDTO);
        return new ResponseEntity<>(userResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUserById(
            @PathVariable
            Integer userId
    ) throws UserNotFoundException {
        userService.deleteUserByUserId(userId);
        return ResponseEntity.noContent().build();
    }
}
