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
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service("authService")
public class AuthServiceImpl implements AuthService {
    private final Environment environment;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenHasher tokenHasher;

    public AuthServiceImpl(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            Environment environment,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            TokenHasher tokenHasher
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.environment = environment;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenHasher = tokenHasher;
    }

    @Override
    @Transactional
    public UserResponseDTO registerUser(
            RegisterRequestDTO registerRequestDTO
    ) {
        if(userRepository.findByEmail(registerRequestDTO.email()).isPresent()) {
            throw new UserAlreadyExistsException(
                    environment.getProperty("Service.USER_ALREADY_EXISTS")
            );
        }
        String hashedPassword = passwordEncoder.encode(registerRequestDTO.password());
        User newUser = new User();
        newUser.setUserName(registerRequestDTO.username());
        newUser.setEmail(registerRequestDTO.email());
        newUser.setPassword(hashedPassword);
        User savedUser = userRepository.save(newUser);
        return new UserResponseDTO(
                savedUser.getUserId(),
                savedUser.getUserName(),
                savedUser.getEmail()
        );
    }

    @Override
    @Transactional
    public AuthResponseDTO loginUser(
            LoginRequestDTO loginRequestDTO
    ) {
        User foundUser = userRepository.findByEmail(loginRequestDTO.email())
                .orElseThrow(
                        () -> new InvalidCredentialsException(
                                environment.getProperty("Service.INVALID_CREDENTIALS")
                        )
                );
        boolean matches = passwordEncoder.matches(loginRequestDTO.password(), foundUser.getPassword());
        if(!matches) {
            throw new InvalidCredentialsException(
                    environment.getProperty("Service.INVALID_CREDENTIALS")
            );
        }
        String token = jwtService.generateToken(foundUser.getUserId());
        String rawRefreshToken = UUID.randomUUID().toString();
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setTokenHash(tokenHasher.hash(rawRefreshToken));
        newRefreshToken.setUser(foundUser);
        newRefreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(newRefreshToken);

        return new AuthResponseDTO(
                foundUser.getUserId(),
                foundUser.getUserName(),
                foundUser.getEmail(),
                token,
                rawRefreshToken
        );
    }

    @Override
    @Transactional
    public AuthResponseDTO refreshToken(
            RefreshTokenRequestDTO refreshTokenRequestDTO
    ) {
        RefreshToken foundRefreshToken = refreshTokenRepository.findByTokenHash(
                tokenHasher.hash(
                        refreshTokenRequestDTO.refreshToken()
                )
        ).orElseThrow(() -> new InvalidRefreshTokenException(
                environment.getProperty("Service.INVALID_REFRESH_TOKEN")
        ));
        if(foundRefreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(foundRefreshToken);
            throw new InvalidRefreshTokenException(
                    environment.getProperty("Service.INVALID_REFRESH_TOKEN")
            );
        }
        User tokenUser = foundRefreshToken.getUser();
        refreshTokenRepository.delete(foundRefreshToken);
        String rawRefreshToken = UUID.randomUUID().toString();
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setTokenHash(
                tokenHasher.hash(rawRefreshToken)
        );
        newRefreshToken.setUser(tokenUser);
        newRefreshToken.setExpiryDate(LocalDateTime.now().plusDays(7));

        refreshTokenRepository.save(newRefreshToken);
        String newAccessToken = jwtService.generateToken(tokenUser.getUserId());

        return new AuthResponseDTO(
                tokenUser.getUserId(),
                tokenUser.getUserName(),
                tokenUser.getEmail(),
                newAccessToken,
                rawRefreshToken
        );
    }
}
