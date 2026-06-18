package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.UserCreateRequestDTO;
import com.wpn.personallibrarytracker.dto.UserResponseDTO;
import com.wpn.personallibrarytracker.dto.UserUpdateRequestDTO;
import com.wpn.personallibrarytracker.entity.User;
import com.wpn.personallibrarytracker.exceptions.UserAlreadyExistsException;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("userService")
public class UserServiceImpl implements UserService {
    private final Environment environment;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, Environment environment) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.environment = environment;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Integer userId) throws UserNotFoundException {
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        environment.getProperty("Service.USER_NOT_FOUND")
                ));
        return new UserResponseDTO(
                foundUser.getUserId(),
                foundUser.getUserName(),
                foundUser.getEmail()
        );
    }

    @Override
    @Transactional
    public UserResponseDTO postUserDetails(UserCreateRequestDTO userCreateRequestDTO) throws UserAlreadyExistsException {
        if(userRepository.findByEmail(userCreateRequestDTO.email()).isPresent()) {
            throw new UserAlreadyExistsException(
                    environment.getProperty("Service.USER_ALREADY_EXISTS")
            );
        }
        User newUser = new User();
        newUser.setUserName(userCreateRequestDTO.userName());
        newUser.setEmail(userCreateRequestDTO.email());
        newUser.setPassword(userCreateRequestDTO.password());
        User savedUser = userRepository.save(newUser);
        return new UserResponseDTO(
                savedUser.getUserId(),
                savedUser.getUserName(),
                savedUser.getEmail()
        );
    }

    @Override
    @Transactional
    public UserResponseDTO putUserDetails(Integer userId, UserUpdateRequestDTO userUpdateRequestDTO) throws UserNotFoundException {
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        environment.getProperty("Service.USER_NOT_FOUND")
                ));
        foundUser.setUserName(userUpdateRequestDTO.userName());
        foundUser.setEmail(userUpdateRequestDTO.email());
        return new UserResponseDTO(
                foundUser.getUserId(),
                foundUser.getUserName(),
                foundUser.getEmail()
        );
    }
}
