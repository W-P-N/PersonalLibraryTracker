package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.UserRequestDTO;
import com.wpn.personallibrarytracker.dto.UserResponseDTO;
import com.wpn.personallibrarytracker.exceptions.UserAlreadyExistsException;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        UserRequestDTO request = new UserRequestDTO("testuser", "test@mail.com", "password123");
        UserResponseDTO response = new UserResponseDTO(1, "testuser", "test@mail.com");

        Mockito.when(userService.postUserDetails(any(UserRequestDTO.class))).thenReturn(response);

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
        UserRequestDTO request = new UserRequestDTO("testuser", "test@mail.com", "password123");

        Mockito.when(userService.postUserDetails(any(UserRequestDTO.class)))
                .thenThrow(new UserAlreadyExistsException("Email already exists"));

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void postUserDetails_shouldReturn400_whenInvalidBody() throws Exception {
        UserRequestDTO request = new UserRequestDTO("testuser", "", "password123");

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
}
