package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.UserRequestDTO;
import com.wpn.personallibrarytracker.dto.UserResponseDTO;
import com.wpn.personallibrarytracker.entity.User;
import com.wpn.personallibrarytracker.exceptions.UserAlreadyExistsException;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("userService")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserDetails(Integer userId) throws UserNotFoundException {
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Service.USER_NOT_FOUND"));
        return modelMapper.map(foundUser, UserResponseDTO.class);
    }

    @Override
    @Transactional
    public UserResponseDTO postUserDetails(UserRequestDTO userRequestDTO) throws UserAlreadyExistsException {
        if(userRepository.findByEmailId(userRequestDTO.email()).isPresent()) {
            throw new UserAlreadyExistsException("Service.USER_ALREADY_EXISTS");
        }
        User newUser = modelMapper.map(userRequestDTO, User.class);
        User savedUser = userRepository.save(newUser);
        return modelMapper.map(savedUser, UserResponseDTO.class);
    }
}
