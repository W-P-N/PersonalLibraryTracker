package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.UserCreateRequestDTO;
import com.wpn.personallibrarytracker.dto.UserResponseDTO;
import com.wpn.personallibrarytracker.dto.UserUpdateRequestDTO;
import com.wpn.personallibrarytracker.exceptions.UserAlreadyExistsException;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;

public interface UserService {
    UserResponseDTO getUserById(Integer userId) throws UserNotFoundException;
    UserResponseDTO postUserDetails(UserCreateRequestDTO userCreateRequestDTO) throws UserAlreadyExistsException;
    UserResponseDTO putUserDetails(Integer userId, UserUpdateRequestDTO userUpdateRequestDTO) throws UserNotFoundException;
}
