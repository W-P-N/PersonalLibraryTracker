package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.readingSessionDTOs.ReadingSessionRequestDTO;
import com.wpn.personallibrarytracker.dto.readingSessionDTOs.ReadingSessionResponseDTO;
import com.wpn.personallibrarytracker.exceptions.BookNotFoundForUserException;
import com.wpn.personallibrarytracker.service.ReadingSessionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReadingSessionController.class)
public class ReadingSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReadingSessionService readingSessionService;

    @Autowired
    private ObjectMapper objectMapper;

    // Happy Paths

    @Test
    void addSession_shouldReturnCreatedSession() throws Exception {
        ReadingSessionRequestDTO request = new ReadingSessionRequestDTO(50);
        ReadingSessionResponseDTO response = new ReadingSessionResponseDTO(1, 50, 50, LocalDateTime.now());

        Mockito.when(readingSessionService.logSession(eq(1), eq(100), any(ReadingSessionRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/users/1/books/100/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.readingSessionId").value(1))
                .andExpect(jsonPath("$.endSessionPageNumber").value(50));
    }

    @Test
    void getSessions_shouldReturnListOfSessionsWithPagination() throws Exception {
        ReadingSessionResponseDTO session1 = new ReadingSessionResponseDTO(1, 50, 50, LocalDateTime.now());
        ReadingSessionResponseDTO session2 = new ReadingSessionResponseDTO(2, 20, 70, LocalDateTime.now());

        org.springframework.data.domain.Page<ReadingSessionResponseDTO> page = new org.springframework.data.domain.PageImpl<>(List.of(session1, session2));

        Mockito.when(readingSessionService.getSessions(eq(1), eq(100), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/users/1/books/100/sessions")
                .param("pageNumber", "0")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].readingSessionId").value(1))
                .andExpect(jsonPath("$.content[1].readingSessionId").value(2));

        Mockito.verify(readingSessionService).getSessions(eq(1), eq(100), org.mockito.ArgumentMatchers.argThat(pageable -> 
                pageable.getPageNumber() == 0 && pageable.getPageSize() == 10
        ));
    }

    @Test
    void getSessionById_shouldReturnSessionDetails() throws Exception {
        ReadingSessionResponseDTO response = new ReadingSessionResponseDTO(1, 50, 50, LocalDateTime.now());

        Mockito.when(readingSessionService.getSessionById(1, 100, 1)).thenReturn(response);

        mockMvc.perform(get("/users/1/books/100/sessions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readingSessionId").value(1))
                .andExpect(jsonPath("$.endSessionPageNumber").value(50));
    }

    @Test
    void updateSession_shouldReturnUpdatedSession() throws Exception {
        ReadingSessionRequestDTO request = new ReadingSessionRequestDTO(60);
        ReadingSessionResponseDTO response = new ReadingSessionResponseDTO(1, 60, 60, LocalDateTime.now());

        Mockito.when(readingSessionService.updateSession(eq(1), eq(100), eq(1), any(ReadingSessionRequestDTO.class))).thenReturn(response);

        mockMvc.perform(patch("/users/1/books/100/sessions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readingSessionId").value(1))
                .andExpect(jsonPath("$.endSessionPageNumber").value(60));
    }

    @Test
    void deleteSession_shouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(readingSessionService).deleteSession(1, 100, 1);

        mockMvc.perform(delete("/users/1/books/100/sessions/1"))
                .andExpect(status().isNoContent());
    }

    // Unhappy Paths

    @Test
    void addSession_shouldReturn404_whenBookNotFound() throws Exception {
        ReadingSessionRequestDTO request = new ReadingSessionRequestDTO(50);

        Mockito.when(readingSessionService.logSession(eq(1), eq(100), any(ReadingSessionRequestDTO.class)))
                .thenThrow(new BookNotFoundForUserException("Book not found"));

        mockMvc.perform(post("/users/1/books/100/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSessionById_shouldReturn404_whenBookNotFound() throws Exception {
        Mockito.when(readingSessionService.getSessionById(1, 100, 1))
                .thenThrow(new BookNotFoundForUserException("Book not found"));

        mockMvc.perform(get("/users/1/books/100/sessions/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSession_shouldReturn500_whenServiceThrowsGenericException() throws Exception {
        ReadingSessionRequestDTO request = new ReadingSessionRequestDTO(60);

        Mockito.when(readingSessionService.updateSession(eq(1), eq(100), eq(1), any(ReadingSessionRequestDTO.class)))
                .thenThrow(new RuntimeException("Generic error"));

        mockMvc.perform(patch("/users/1/books/100/sessions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}
