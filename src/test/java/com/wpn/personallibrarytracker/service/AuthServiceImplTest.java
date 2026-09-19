package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.authDTOs.AuthResponseDTO;
import com.wpn.personallibrarytracker.dto.authDTOs.LoginRequestDTO;
import com.wpn.personallibrarytracker.dto.authDTOs.RefreshTokenRequestDTO;
import com.wpn.personallibrarytracker.dto.authDTOs.RegisterRequestDTO;
import com.wpn.personallibrarytracker.dto.userDTOs.UserResponseDTO;
import com.wpn.personallibrarytracker.entity.RefreshToken;
import com.wpn.personallibrarytracker.entity.User;
import com.wpn.personallibrarytracker.exceptions.InvalidCredentialsException;
import com.wpn.personallibrarytracker.exceptions.InvalidRefreshTokenException;
import com.wpn.personallibrarytracker.exceptions.UserAlreadyExistsException;
import com.wpn.personallibrarytracker.repository.RefreshTokenRepository;
import com.wpn.personallibrarytracker.repository.UserRepository;
import com.wpn.personallibrarytracker.utility.TokenHasher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {
    @Mock
    UserRepository userRepository;
    @Mock
    RefreshTokenRepository refreshTokenRepository;
    @InjectMocks
    AuthServiceImpl authService;
    @Mock
    JwtService jwtService;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    TokenHasher tokenHasher;
    @Mock
    MessageSource messageSource;

    @Test
    void registerUser_happyPath_shouldReturnUserResponseDTO() {
        // Arrange
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO(
                "test",
                "test@mail.com",
                "test@123"
        );
        User newUser = new User();
        newUser.setUserName("test");
        newUser.setEmail("test@mail.com");
        String hashedPassword = "asfgart";
        newUser.setPassword(hashedPassword);
        newUser.setUserId(1);
        Mockito.when(userRepository.findByEmail(Mockito.anyString()))
                .thenReturn(Optional.empty());
        Mockito.when(passwordEncoder.encode(Mockito.anyString()))
                .thenReturn(hashedPassword);
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(newUser);
        // Act
        UserResponseDTO userResponseDTO = authService.registerUser(
                registerRequestDTO
        );
        // Assert
        Assertions.assertEquals("test", userResponseDTO.userName());
        Assertions.assertEquals("test@mail.com", userResponseDTO.email());
    }

    @Test
    void registerUser_unHappyPath_shouldThrowUserAlreadyExistsException() {
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO(
                "test",
                "test@mail.com",
                "test@123"
        );
        User user = new User();
        Mockito.when(userRepository.findByEmail(Mockito.anyString()))
                .thenReturn(Optional.of(user));

        Assertions.assertThrows(UserAlreadyExistsException.class, () -> {
            authService.registerUser(registerRequestDTO);
        });
    }

    @Test
    void loginUser_happyPath_shouldReturnAuthResponseDTO() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(
                "test@mail.com",
                "test@123"
        );
        User user = new User();
        user.setUserId(1);
        user.setUserName("test");
        user.setEmail("test@mail.com");
        user.setPassword("hashed");

        Mockito.when(userRepository.findByEmail(Mockito.anyString()))
                .thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(jwtService.generateToken(Mockito.anyInt()))
                .thenReturn("dummy-jwt-token");
        
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash("dummy-refresh-token");
        Mockito.when(refreshTokenRepository.save(Mockito.any(RefreshToken.class)))
                .thenReturn(refreshToken);
        Mockito.when(tokenHasher.hash(Mockito.anyString()))
                .thenReturn("asaw");

        AuthResponseDTO responseDTO = authService.loginUser(loginRequestDTO);

        Assertions.assertEquals("test", responseDTO.username());
        Assertions.assertEquals("dummy-jwt-token", responseDTO.token());
        Assertions.assertNotNull(responseDTO.refreshToken());
    }

    @Test
    void loginUser_unHappyPath_shouldThrowInvalidCredentialsException_whenUserNotFoundForEmail() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(
                "test@mail.com",
                "test@123"
        );
        Mockito.when(userRepository.findByEmail(Mockito.anyString()))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(InvalidCredentialsException.class, () -> {
            authService.loginUser(loginRequestDTO);
        });
    }

    @Test
    void loginUser_unHappyPath_shouldThrowInvalidCredentialsException_whenPasswordDoesNotMatch() {
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(
                "test@mail.com",
                "test@123"
        );
        User user = new User();
        user.setPassword("hashed");
        Mockito.when(userRepository.findByEmail(Mockito.anyString()))
                .thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(false);

        Assertions.assertThrows(InvalidCredentialsException.class, () -> {
            authService.loginUser(loginRequestDTO);
        });
    }

    @Test
    void refreshToken_happyPath_shouldReturnAuthResponseDTO() {
        RefreshTokenRequestDTO requestDTO = new RefreshTokenRequestDTO("old-refresh-token");
        RefreshToken oldToken = new RefreshToken();
        oldToken.setTokenHash("old-refresh-token");
        oldToken.setExpiryDate(LocalDateTime.now().plusDays(1));
        User user = new User();
        user.setUserId(1);
        user.setUserName("test");
        oldToken.setUser(user);

        Mockito.when(tokenHasher.hash(Mockito.anyString()))
                .thenReturn("old-refresh-token");
        Mockito.when(refreshTokenRepository.findByTokenHash(Mockito.anyString()))
                .thenReturn(Optional.of(oldToken));
        
        RefreshToken newToken = new RefreshToken();
        newToken.setTokenHash("new-refresh-token");
        Mockito.when(refreshTokenRepository.save(Mockito.any(RefreshToken.class)))
                .thenReturn(newToken);
        Mockito.when(jwtService.generateToken(Mockito.anyInt()))
                .thenReturn("new-jwt-token");

        AuthResponseDTO responseDTO = authService.refreshToken(requestDTO);

        Assertions.assertEquals("new-jwt-token", responseDTO.token());
        Assertions.assertNotNull(responseDTO.refreshToken());
    }

    @Test
    void refreshToken_unHappyPath_shouldThrowInvalidRefreshTokenException_whenTokenNotFound() {
        RefreshTokenRequestDTO requestDTO = new RefreshTokenRequestDTO("non-existent-token");
        Mockito.when(tokenHasher.hash(Mockito.anyString()))
                .thenReturn("non-existent-token");
        Mockito.when(refreshTokenRepository.findByTokenHash(Mockito.anyString()))
                .thenReturn(Optional.empty());

        Assertions.assertThrows(InvalidRefreshTokenException.class, () -> {
            authService.refreshToken(requestDTO);
        });
    }

    @Test
    void refreshToken_unHappyPath_shouldThrowInvalidRefreshTokenException_whenTokenIsExpired() {
        RefreshTokenRequestDTO requestDTO = new RefreshTokenRequestDTO("expired-token");
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setTokenHash("expired-token");
        expiredToken.setExpiryDate(LocalDateTime.now().minusDays(1));

        Mockito.when(tokenHasher.hash(Mockito.anyString()))
                .thenReturn("expired-token");
        Mockito.when(refreshTokenRepository.findByTokenHash(Mockito.anyString()))
                .thenReturn(Optional.of(expiredToken));

        Assertions.assertThrows(InvalidRefreshTokenException.class, () -> {
            authService.refreshToken(requestDTO);
        });
    }
}
