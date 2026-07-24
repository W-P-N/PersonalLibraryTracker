package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.authDTOs.LoginRequestDTO;
import com.wpn.personallibrarytracker.dto.authDTOs.LoginResponseDTO;
import com.wpn.personallibrarytracker.dto.authDTOs.RegisterRequestDTO;
import com.wpn.personallibrarytracker.dto.userDTOs.UserCreateRequestDTO;
import com.wpn.personallibrarytracker.dto.userDTOs.UserResponseDTO;
import com.wpn.personallibrarytracker.entity.User;
import com.wpn.personallibrarytracker.exceptions.InvalidCredentialsException;
import com.wpn.personallibrarytracker.exceptions.UserAlreadyExistsException;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.repository.UserRepository;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("authService")
public class AuthServiceImpl implements AuthService {
    private final Environment environment;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UserRepository userRepository,
            Environment environment,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.environment = environment;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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
    @Transactional(readOnly = true)
    public LoginResponseDTO loginUser(
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
        return new LoginResponseDTO(
                foundUser.getUserId(),
                foundUser.getUserName(),
                foundUser.getEmail(),
                token
        );
    }
}
