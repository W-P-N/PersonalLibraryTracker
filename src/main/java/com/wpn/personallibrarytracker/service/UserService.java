package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.userDTOs.UserCreateRequestDTO;
import com.wpn.personallibrarytracker.dto.userDTOs.UserResponseDTO;
import com.wpn.personallibrarytracker.dto.userDTOs.UserUpdateRequestDTO;
import com.wpn.personallibrarytracker.exceptions.UserAlreadyExistsException;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;

public interface UserService {
    UserResponseDTO getUser(Integer userId) throws UserNotFoundException;
    UserResponseDTO registerUser(UserCreateRequestDTO userCreateRequestDTO) throws UserAlreadyExistsException;
    UserResponseDTO updateUser(Integer userId, UserUpdateRequestDTO userUpdateRequestDTO) throws UserNotFoundException;
    void deleteUser(Integer userId) throws UserNotFoundException;
}
