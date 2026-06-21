package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.UserCreateRequestDTO;
import com.wpn.personallibrarytracker.dto.UserResponseDTO;
import com.wpn.personallibrarytracker.dto.UserUpdateRequestDTO;
import com.wpn.personallibrarytracker.exceptions.UserAlreadyExistsException;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.service.UserService;
import netscape.javascript.JSObject;
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

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void postUserDetails_shouldReturn201AndUserResponseDTO_whenValidBody() throws Exception {
        UserCreateRequestDTO request = new UserCreateRequestDTO("testuser", "test@mail.com", "password123");
        UserResponseDTO response = new UserResponseDTO(1, "testuser", "test@mail.com");

        Mockito.when(userService.postUserDetails(any(UserCreateRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.userName").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@mail.com"));
    }

    @Test
    void postUserDetails_shouldReturn409_whenDuplicateEmail() throws Exception {
        UserCreateRequestDTO request = new UserCreateRequestDTO("testuser", "test@mail.com", "password123");

        Mockito.when(userService.postUserDetails(any(UserCreateRequestDTO.class)))
                .thenThrow(new UserAlreadyExistsException("Email already exists"));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void postUserDetails_shouldReturn400_whenInvalidBody() throws Exception {
        UserCreateRequestDTO request = new UserCreateRequestDTO("testuser", "", "password123");

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void getUserDetails_shouldReturn200AndUserResponseDTO_whenFound() throws Exception {
        UserResponseDTO response = new UserResponseDTO(1, "testuser", "test@mail.com");

        Mockito.when(userService.getUserById(1)).thenReturn(response);

        mockMvc.perform(get("/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.userName").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@mail.com"));
    }

    @Test
    void getUserDetails_shouldReturn404_whenNotFound() throws Exception {
        Mockito.when(userService.getUserById(1)).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void putUserDetails_shouldReturn200AndUserResponseDTO_whenUserFound() throws Exception {
        UserUpdateRequestDTO userUpdateRequestDTO = new UserUpdateRequestDTO("test1", "test@123.com");
        UserResponseDTO userResponseDTO = new UserResponseDTO(1, "test1", "test@123.com");
        Mockito.when(userService.putUserDetails(1, userUpdateRequestDTO)).thenReturn(userResponseDTO);
        mockMvc.perform(put("/users/{userId}", 1)
                .content(objectMapper.writeValueAsString(userUpdateRequestDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.userName").value("test1"))
                .andExpect(jsonPath("$.email").value("test@123.com"));
    }

    @Test
    void putUserDetails_shouldReturn404_whenNotFound() throws Exception {
        UserUpdateRequestDTO userUpdateRequestDTO = new UserUpdateRequestDTO("test1", "test@123.com");
        Mockito.when(userService.putUserDetails(123, userUpdateRequestDTO))
                .thenThrow(new UserNotFoundException("User not found"));
        mockMvc.perform(put("/users/{userId}", 123)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpdateRequestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUserById_shouldReturn204() throws Exception {
        Integer mockUserId = 100;
        mockMvc.perform(delete("/users/{userId}", mockUserId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        Mockito.verify(userService).deleteUserByUserId(mockUserId);
    }

    @Test
    void deleteUserById_shouldReturn404_whenNotFound() throws Exception {
        Integer mockUserId = 123;
        Mockito.doThrow(new UserNotFoundException("User not found"))
                .when(userService).deleteUserByUserId(mockUserId);
        mockMvc.perform(delete("/users/{userId}", mockUserId))
                .andExpect(status().isNotFound());
    }
}
