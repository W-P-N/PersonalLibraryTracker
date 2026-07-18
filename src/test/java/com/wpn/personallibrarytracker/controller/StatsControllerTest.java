package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import com.wpn.personallibrarytracker.dto.statsDTOs.StatsResponseDTO;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.service.StatsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
public class StatsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatsService statsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getStats_happyPath_shouldReturnStats() throws Exception {
        // Arrange
        Integer userId = 1;
        StatsResponseDTO mockResponse = new StatsResponseDTO(
                10L, 2L, 3L, 5L, 1000L, null, 4.5, 3L
        );
        Mockito.when(statsService.getStats(userId)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBooks").value(10))
                .andExpect(jsonPath("$.booksNotStarted").value(2))
                .andExpect(jsonPath("$.booksReading").value(3))
                .andExpect(jsonPath("$.booksFinished").value(5))
                .andExpect(jsonPath("$.totalPagesRead").value(1000))
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.currentStreak").value(3));
    }

    @Test
    void getStats_unhappyPath_whenUserNotFound() throws Exception {
        // Arrange
        Integer userId = 999;
        Mockito.when(statsService.getStats(userId)).thenThrow(new UserNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/users/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorMessage").value("User not found"));
    }
}
