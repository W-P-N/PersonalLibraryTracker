package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.userDTOs.UserResponseDTO;
import com.wpn.personallibrarytracker.dto.userDTOs.UserUpdateRequestDTO;
import com.wpn.personallibrarytracker.entity.User;
import com.wpn.personallibrarytracker.exceptions.ResourceNotFoundException;
import com.wpn.personallibrarytracker.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    UserRepository userRepository;
    @InjectMocks
    UserServiceImpl userService;
    @Mock
    Environment environment;

    @Test
    void getUserById_shouldReturnUserResponseDTO_whenUserIdIsFound() {
        User foundUser = new User();
        foundUser.setUserId(12345);
        foundUser.setUserName("test");
        foundUser.setPassword("testPassword");
        foundUser.setEmail("test@mail.com");

        UserResponseDTO userResponseDTO = new UserResponseDTO(
                foundUser.getUserId(),
                foundUser.getUserName(),
                foundUser.getEmail()
        );

        Mockito.when(userRepository.findById(12345))
                .thenReturn(Optional.of(foundUser));

        Assertions.assertEquals(userResponseDTO, userService.getUser(12345));
        Mockito.verify(userRepository, Mockito.times(1)).findById(12345);
    }

    @Test
    void getUserById_shouldThrowResourceNotFoundException_whenUserIdIsNotFound() {
        Mockito.when(userRepository.findById(12345))
                .thenReturn(Optional.empty());
        Mockito.when(environment.getProperty("Service.RESOURCE_NOT_FOUND"))
                .thenReturn("The requested resource was not found");

        Assertions.assertThrows(ResourceNotFoundException.class, () -> userService.getUser(12345));
        Mockito.verify(userRepository, Mockito.times(1)).findById(12345);
    }

    @Test
    void updateUserDetailsById_shouldReturnUserResponseDTO_whenUserIdIsFound() {
        User foundUser = new User();
        foundUser.setUserId(12345);
        foundUser.setUserName("test");
        foundUser.setPassword("testPassword");
        foundUser.setEmail("test@mail.com");

        UserUpdateRequestDTO userUpdateRequestDTO = new UserUpdateRequestDTO(
                "test",
                "test@123.com"
        );

        UserResponseDTO userResponseDTO = new UserResponseDTO(
                foundUser.getUserId(),
                userUpdateRequestDTO.userName(),
                userUpdateRequestDTO.email()
        );

        Mockito.when(userRepository.findById(12345))
                .thenReturn(Optional.of(foundUser));

        UserResponseDTO expectedUserResponseDTO = userService.updateUser(12345, userUpdateRequestDTO);

        Assertions.assertEquals(userResponseDTO, expectedUserResponseDTO);
        Mockito.verify(userRepository, Mockito.times(1)).findById(12345);
    }

    @Test
    void updateUserDetailsById_shouldThrowResourceNotFoundException_whenUserIdIsNotFound() {
        Mockito.when(userRepository.findById(123))
                .thenReturn(Optional.empty());
        Mockito.when(environment.getProperty("Service.RESOURCE_NOT_FOUND"))
                .thenReturn("The requested resource was not found");

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(123, new UserUpdateRequestDTO("test", "test@123.com"));
        });
        Mockito.verify(userRepository, Mockito.times(1)).findById(123);
    }

    @Test
    void deleteUserById_shouldDeleteUser_whenUserExists() {
        Integer userId = 1;
        User mockUser = new User();
        mockUser.setUserId(userId);
        mockUser.setUserName("Test");

        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(mockUser));
        Mockito.doNothing().when(userRepository).delete(mockUser);
        userService.deleteUser(userId);

        Mockito.verify(userRepository, Mockito.times(1)).findById(userId);
        Mockito.verify(userRepository, Mockito.times(1)).delete(mockUser);
    }

    @Test
    void deleteUserById_shouldThrowResourceNotFoundException_whenUserIdIsNotFound() {
        Mockito.when(userRepository.findById(12345))
                .thenReturn(Optional.empty());
        Mockito.when(environment.getProperty("Service.RESOURCE_NOT_FOUND"))
                .thenReturn("The requested resource was not found");

        Assertions.assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(12345));
        Mockito.verify(userRepository, Mockito.times(1)).findById(12345);
    }
}
