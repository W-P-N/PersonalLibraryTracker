package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.noteDTOs.NoteDetailsResponseDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteRequestDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteResponseDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteUpdateRequestDTO;
import com.wpn.personallibrarytracker.exceptions.ResourceNotFoundException;
import com.wpn.personallibrarytracker.service.NoteService;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.wpn.personallibrarytracker.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.Import;
import com.wpn.personallibrarytracker.config.SecurityConfig;

@WebMvcTest(NoteController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
public class NoteControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NoteService noteService;

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

    // --- Add Note Tests ---
    @Test
    void addNote_happyPath_shouldReturnCreatedNote() throws Exception {
        NoteRequestDTO request = new NoteRequestDTO("Test Note", 5);
        NoteDetailsResponseDTO response = new NoteDetailsResponseDTO(1, "Test Note", LocalDateTime.now(), 5);

        when(noteService.createNote(eq(1), eq(1), any(NoteRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/books/1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.noteId").value(1))
                .andExpect(jsonPath("$.content").value("Test Note"));
    }

    @Test
    void addNote_unHappyPath_shouldReturn404_whenUserNotFound() throws Exception {
        NoteRequestDTO request = new NoteRequestDTO("Test Note", 5);
        when(noteService.createNote(eq(1), eq(1), any(NoteRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(post("/books/1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addNote_unHappyPath_shouldReturn404_whenBookNotFoundForUser() throws Exception {
        NoteRequestDTO request = new NoteRequestDTO("Test Note", 5);
        when(noteService.createNote(eq(1), eq(1), any(NoteRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Book not found"));

        mockMvc.perform(post("/books/1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // --- Get Notes Tests
    @Test
    void getNotes_happyPath_shouldReturnPageOfNotes() throws Exception {
        NoteResponseDTO noteResponse = new NoteResponseDTO(1, LocalDateTime.now(), 5);
        Page<NoteResponseDTO> page = new PageImpl<>(Collections.singletonList(noteResponse));

        when(noteService.getNotes(eq(1), eq(1), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/books/1/notes")
                        .param("pageNumber", "0")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].noteId").value(1));
    }

    @Test
    void getNotes_unHappyPath_shouldThrow404_whenUserNotFound() throws Exception {
        when(noteService.getNotes(eq(1), eq(1), any(Pageable.class)))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/books/1/notes"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getNotes_unHappyPath_shouldThrow404_whenBookNotFoundForUser() throws Exception {
        when(noteService.getNotes(eq(1), eq(1), any(Pageable.class)))
                .thenThrow(new ResourceNotFoundException("Book not found"));

        mockMvc.perform(get("/books/1/notes"))
                .andExpect(status().isNotFound());
    }

    // --- Get Note By Id Tests ---
    @Test
    void getNoteById_happyPath_shouldReturnNoteWithRequestedId() throws Exception {
        NoteDetailsResponseDTO response = new NoteDetailsResponseDTO(1, "Test Content", LocalDateTime.now(), 10);
        when(noteService.getNoteById(1, 1, 1)).thenReturn(response);

        mockMvc.perform(get("/books/1/notes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noteId").value(1))
                .andExpect(jsonPath("$.content").value("Test Content"));
    }

    @Test
    void getNoteById_unHappyPath_shouldThrow404ResourceNotFoundException_whenNotNotFound() throws Exception {
        when(noteService.getNoteById(1, 1, 1))
                .thenThrow(new ResourceNotFoundException("Note not found"));

        mockMvc.perform(get("/books/1/notes/1"))
                .andExpect(status().isNotFound());
    }

    // -- Update Note Tests ---
    @Test
    void updateNote_happyPath_shouldReturnUpdatedNote() throws Exception {
        NoteUpdateRequestDTO request = new NoteUpdateRequestDTO("Updated Note", 15);
        NoteDetailsResponseDTO response = new NoteDetailsResponseDTO(1, "Updated Note", LocalDateTime.now(), 15);

        when(noteService.updateNote(eq(1), eq(1), eq(1), any(NoteUpdateRequestDTO.class))).thenReturn(response);

        mockMvc.perform(patch("/books/1/notes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated Note"));
    }

    @Test
    void updateNote_unHappyPath_shouldThrow404ResourceNotFoundException_whenUserNotFound() throws Exception {
        NoteUpdateRequestDTO request = new NoteUpdateRequestDTO("Updated Note", 15);
        when(noteService.updateNote(eq(1), eq(1), eq(1), any(NoteUpdateRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(patch("/books/1/notes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateNote_unHappyPath_shouldThrow404ResourceNotFoundException() throws Exception {
        NoteUpdateRequestDTO request = new NoteUpdateRequestDTO("Updated Note", 15);
        when(noteService.updateNote(eq(1), eq(1), eq(1), any(NoteUpdateRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Book not found"));

        mockMvc.perform(patch("/books/1/notes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateNote_unHappyPath_shouldThrow404ResourceNotFoundException_whenNoteNotFound() throws Exception {
        NoteUpdateRequestDTO request = new NoteUpdateRequestDTO("Updated Note", 15);
        when(noteService.updateNote(eq(1), eq(1), eq(1), any(NoteUpdateRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Note not found"));

        mockMvc.perform(patch("/books/1/notes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // --- Delete Note Tests ---
    @Test
    void deleteNote_happyPath_shouldDeleteNoteWithGivenId() throws Exception {
        doNothing().when(noteService).deleteNote(1, 1, 1);

        mockMvc.perform(delete("/books/1/notes/1"))
                .andExpect(status().isNoContent());

        verify(noteService, times(1)).deleteNote(1, 1, 1);
    }

    @Test
    void deleteNote_unHappyPath_shouldThrow404ResourceNotFoundException() throws Exception {
        doThrow(new ResourceNotFoundException("Note not found")).when(noteService).deleteNote(1, 1, 1);

        mockMvc.perform(delete("/books/1/notes/1"))
                .andExpect(status().isNotFound());
    }
}
