package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.UserRequestDTO;
import com.wpn.personallibrarytracker.dto.UserResponseDTO;
import com.wpn.personallibrarytracker.entity.User;
import com.wpn.personallibrarytracker.exceptions.UserAlreadyExistsException;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.repository.UserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    UserRepository userRepository;
    @Mock
    ModelMapper modelMapper;
    @InjectMocks
    UserServiceImpl userService;

    @Test
    void postUserDetails_shouldReturnUserResponseDTO_whenEmailIsUnique() {
        // Arrange
        UserRequestDTO userRequestDTO = new UserRequestDTO(
                "test",
                "test@mail.com",
                "testpassword"
        );

        User user = new User();
        user.setUserName("test");
        user.setEmail("test@mail.com");
        user.setPassword("testpassword");

        UserResponseDTO newUser = new UserResponseDTO(
                12345,
                "test",
                "test@mail.com"
        );

        Mockito.when(userRepository.findByEmail(Mockito.anyString()))
                    .thenReturn(Optional.empty());
        Mockito.when(modelMapper.map(Mockito.any(UserRequestDTO.class), Mockito.eq(User.class)))
                .thenReturn(user);
        Mockito.when(modelMapper.map(Mockito.any(User.class), Mockito.eq(UserResponseDTO.class)))
                .thenReturn(newUser);
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(user);

        // Act
        UserResponseDTO userResponseDTO = userService.postUserDetails(userRequestDTO);

        Assertions.assertEquals(userResponseDTO, newUser);
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verify(userRepository, Mockito.times(1)).findByEmail(Mockito.anyString());
    }

    @Test
    void postUserDetails_shouldThrowUserAlreadyExistsException_whenEmailExists() {
        // Arrange
        User user = new User();
        user.setUserName("test");
        user.setEmail("test@mail.com");
        user.setPassword("testpassword");

        Optional<User> userOptional = Optional.of(user);

        Mockito.when(userRepository.findByEmail(Mockito.anyString()))
                .thenReturn(userOptional);

        UserRequestDTO userRequestDTO = new UserRequestDTO(
                "test",
                "test@mail.com",
                "testpassword"
        );
        // Act and Assert
        Assertions.assertThrows(UserAlreadyExistsException.class, () -> {
            userService.postUserDetails(userRequestDTO);
        });
    }

    @Test
    void getUserById_shouldReturnUserResponseDTO_whenUserIdIsFound() {
        // Arrange
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

        Mockito.when(userRepository.findById(Mockito.anyInt()))
                .thenReturn(Optional.of(foundUser));
        Mockito.when(modelMapper.map(Mockito.any(User.class), Mockito.eq(UserResponseDTO.class)))
                .thenReturn(userResponseDTO);

        // Act and Assert
        Assertions.assertEquals(
                userService.getUserById(Mockito.anyInt()),
                userResponseDTO
        );
        Mockito.verify(userRepository, Mockito.times(1)).findById(Mockito.anyInt());
    }

    @Test
    void getUserById_shouldThrowUserNotFoundException_whenUserIdIsNotFound() {
        // Arrange
        Mockito.when(userRepository.findById(Mockito.anyInt()))
                .thenReturn(Optional.empty());

        // Act and Assert
        Assertions.assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(12345);
        });
        
        Mockito.verify(userRepository, Mockito.times(1)).findById(Mockito.anyInt());
    }
}
