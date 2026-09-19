package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.userDTOs.UserResponseDTO;
import com.wpn.personallibrarytracker.dto.userDTOs.UserUpdateRequestDTO;
import com.wpn.personallibrarytracker.entity.User;
import com.wpn.personallibrarytracker.exceptions.ResourceNotFoundException;
import com.wpn.personallibrarytracker.repository.UserRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("userService")
public class UserServiceImpl implements UserService {
    private final MessageSource messageSource;
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository, MessageSource messageSource) {
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUser(Integer userId) {
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("Service.RESOURCE_NOT_FOUND", null, LocaleContextHolder.getLocale())
                ));
        return new UserResponseDTO(
                foundUser.getUserId(),
                foundUser.getUserName(),
                foundUser.getEmail()
        );
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(Integer userId, UserUpdateRequestDTO userUpdateRequestDTO) {
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("Service.RESOURCE_NOT_FOUND", null, LocaleContextHolder.getLocale())
                ));
        foundUser.setUserName(userUpdateRequestDTO.userName());
        foundUser.setEmail(userUpdateRequestDTO.email());
        return new UserResponseDTO(
                foundUser.getUserId(),
                foundUser.getUserName(),
                foundUser.getEmail()
        );
    }

    @Override
    @Transactional
    public void deleteUser(Integer userId) {
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("Service.RESOURCE_NOT_FOUND", null, LocaleContextHolder.getLocale())
                ));
        userRepository.delete(foundUser);
    }
}
