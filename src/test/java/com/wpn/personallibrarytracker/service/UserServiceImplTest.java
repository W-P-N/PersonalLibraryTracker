package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.userDTOs.UserCreateRequestDTO;
import com.wpn.personallibrarytracker.dto.userDTOs.UserResponseDTO;
import com.wpn.personallibrarytracker.dto.userDTOs.UserUpdateRequestDTO;
import com.wpn.personallibrarytracker.entity.User;
import com.wpn.personallibrarytracker.exceptions.UserAlreadyExistsException;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    UserRepository userRepository;
    @Mock
    ModelMapper modelMapper;
    @InjectMocks
    UserServiceImpl userService;
    @Mock
    Environment environment;

    @Test
    void registerUserDetails_shouldReturnUserResponseDTO_whenEmailIsUnique() {
        // Arrange
        UserCreateRequestDTO userCreateRequestDTO = new UserCreateRequestDTO(
                "test",
                "test@mail.com",
                "testpassword"
        );

        User user = new User();
        user.setUserId(1234);
        user.setUserName("test");
        user.setEmail("test@mail.com");
        user.setPassword("testpassword");

        UserResponseDTO newUser = new UserResponseDTO(
                1234,
                "test",
                "test@mail.com"
        );

        Mockito.when(userRepository.findByEmail(Mockito.anyString()))
                    .thenReturn(Optional.empty());
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(user);

        // Act
        UserResponseDTO userResponseDTO = userService.registerUser(userCreateRequestDTO);

        Assertions.assertEquals(userResponseDTO, newUser);
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
        Mockito.verify(userRepository, Mockito.times(1)).findByEmail(Mockito.anyString());
    }

    @Test
    void registerUserDetails_shouldThrowUserAlreadyExistsException_whenEmailExists() {
        // Arrange
        User user = new User();
        user.setUserName("test");
        user.setEmail("test@mail.com");
        user.setPassword("testpassword");

        Optional<User> userOptional = Optional.of(user);

        Mockito.when(userRepository.findByEmail(Mockito.anyString()))
                .thenReturn(userOptional);

        UserCreateRequestDTO userCreateRequestDTO = new UserCreateRequestDTO(
                "test",
                "test@mail.com",
                "testpassword"
        );
        // Act and Assert
        Assertions.assertThrows(UserAlreadyExistsException.class, () -> {
            userService.registerUser(userCreateRequestDTO);
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

        // Act and Assert
        Assertions.assertEquals(
                userService.getUser(Mockito.anyInt()),
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
            userService.getUser(12345);
        });
        
        Mockito.verify(userRepository, Mockito.times(1)).findById(Mockito.anyInt());
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

        Mockito.when(userRepository.findById(Mockito.anyInt()))
                .thenReturn(Optional.of(foundUser));

        UserResponseDTO expectedUserResponseDTO = userService.updateUser(Mockito.anyInt(), userUpdateRequestDTO);

        Assertions.assertEquals(expectedUserResponseDTO, userResponseDTO);
        Mockito.verify(userRepository, Mockito.times(1)).findById(Mockito.anyInt());
    }

    @Test
    void updateUserDetailsById_shouldReturnUserNotFound_whenUserIdIsNotFound() {
        Mockito.when(userRepository.findById(Mockito.anyInt()))
                .thenReturn(Optional.empty());
        Assertions.assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(123, new UserUpdateRequestDTO("test", "test@123.com"));
        });
        Mockito.verify(userRepository, Mockito.times(1)).findById(Mockito.anyInt());
    }

    @Test
    void deleteUserById_shouldNothing() {
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
    void deleteUserById_shouldThrowUserNotFoundException_whenUserIdIsNotFound() {
        Mockito.when(userRepository.findById(Mockito.anyInt()))
                .thenReturn(Optional.empty());
        Assertions.assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(Mockito.anyInt());
        });
        Mockito.verify(userRepository, Mockito.times(1)).findById(Mockito.anyInt());
    }
}
