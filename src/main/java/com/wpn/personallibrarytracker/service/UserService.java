package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.UserRequestDTO;
import com.wpn.personallibrarytracker.dto.UserResponseDTO;
import com.wpn.personallibrarytracker.exceptions.UserAlreadyExistsException;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;

public interface UserService {
    UserResponseDTO getUserById(Integer userId) throws UserNotFoundException;
    UserResponseDTO postUserDetails(UserRequestDTO userRequestDTO) throws UserAlreadyExistsException;
}
