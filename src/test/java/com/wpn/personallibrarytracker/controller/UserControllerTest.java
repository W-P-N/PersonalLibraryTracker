package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.userDTOs.UserCreateRequestDTO;
import com.wpn.personallibrarytracker.dto.userDTOs.UserResponseDTO;
import com.wpn.personallibrarytracker.dto.userDTOs.UserUpdateRequestDTO;
import com.wpn.personallibrarytracker.exceptions.ResourceNotFoundException;
import com.wpn.personallibrarytracker.service.UserService;
import com.wpn.personallibrarytracker.service.JwtService;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Import;
import com.wpn.personallibrarytracker.config.SecurityConfig;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(1, null, java.util.Collections.emptyList())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUserDetails_shouldReturn200AndUserResponseDTO_whenFound() throws Exception {
        UserResponseDTO response = new UserResponseDTO(1, "testuser", "test@mail.com");

        Mockito.when(userService.getUser(1)).thenReturn(response);

        mockMvc.perform(get("/users/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.userName").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@mail.com"));
    }

    @Test
    void getUser_shouldReturn404_whenNotFound() throws Exception {
        Mockito.when(userService.getUser(1)).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/users/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUserDetails_shouldReturn200AndUserResponseDTO_whenUserFound() throws Exception {
        UserUpdateRequestDTO userUpdateRequestDTO = new UserUpdateRequestDTO("test1", "test@123.com");
        UserResponseDTO userResponseDTO = new UserResponseDTO(1, "test1", "test@123.com");
        Mockito.when(userService.updateUser(1, userUpdateRequestDTO)).thenReturn(userResponseDTO);
        mockMvc.perform(put("/users/me", 1)
                .content(objectMapper.writeValueAsString(userUpdateRequestDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.userName").value("test1"))
                .andExpect(jsonPath("$.email").value("test@123.com"));
    }

    @Test
    void updateUser_shouldReturn404_whenNotFound() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(123, null, java.util.Collections.emptyList())
        );
        UserUpdateRequestDTO userUpdateRequestDTO = new UserUpdateRequestDTO("test1", "test@123.com");
        Mockito.when(userService.updateUser(123, userUpdateRequestDTO))
                .thenThrow(new ResourceNotFoundException("User not found"));
        mockMvc.perform(put("/users/me", 123)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpdateRequestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_shouldReturn204() throws Exception {
        Integer mockUserId = 100;
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(mockUserId, null, java.util.Collections.emptyList())
        );
        mockMvc.perform(delete("/users/me", mockUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        Mockito.verify(userService).deleteUser(mockUserId);
    }

    @Test
    void deleteUser_shouldReturn404_whenNotFound() throws Exception {
        Integer mockUserId = 123;
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(mockUserId, null, java.util.Collections.emptyList())
        );
        Mockito.doThrow(new ResourceNotFoundException("User not found"))
                .when(userService).deleteUser(mockUserId);
        mockMvc.perform(delete("/users/me", mockUserId))
                .andExpect(status().isNotFound());
    }
}
