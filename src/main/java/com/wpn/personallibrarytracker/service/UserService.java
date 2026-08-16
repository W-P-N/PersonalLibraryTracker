package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.userDTOs.UserCreateRequestDTO;
import com.wpn.personallibrarytracker.dto.userDTOs.UserResponseDTO;
import com.wpn.personallibrarytracker.dto.userDTOs.UserUpdateRequestDTO;
import com.wpn.personallibrarytracker.exceptions.UserAlreadyExistsException;
import com.wpn.personallibrarytracker.exceptions.ResourceNotFoundException;

public interface UserService {
    UserResponseDTO getUser(Integer userId);

    UserResponseDTO updateUser(Integer userId, UserUpdateRequestDTO userUpdateRequestDTO);

    void deleteUser(Integer userId);
}
