package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.BookRequestDTO;
import com.wpn.personallibrarytracker.dto.BookResponseDTO;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.service.BookService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addBook_shouldReturn200AndBookResponseDTO_whenValidInput() throws Exception {
        // Arrange
        Integer userId = 1;
        BookRequestDTO request = new BookRequestDTO(
                "The Hobbit",
                "J.R.R. Tolkien",
                "9780007525492",
                "https://example.com/cover.jpg",
                310
        );

        BookResponseDTO response = new BookResponseDTO(
                101,
                "The Hobbit",
                "J.R.R. Tolkien",
                "9780007525492",
                "https://example.com/cover.jpg",
                310
        );

        Mockito.when(bookService.addBook(eq(userId), any(BookRequestDTO.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/books/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(101))
                .andExpect(jsonPath("$.title").value("The Hobbit"))
                .andExpect(jsonPath("$.author").value("J.R.R. Tolkien"))
                .andExpect(jsonPath("$.isbn").value("9780007525492"))
                .andExpect(jsonPath("$.coverUrl").value("https://example.com/cover.jpg"))
                .andExpect(jsonPath("$.totalPages").value(310));
    }

    @Test
    void addBook_shouldReturn404_whenUserNotFound() throws Exception {
        // Arrange
        Integer userId = 999;
        BookRequestDTO request = new BookRequestDTO(
                "The Hobbit",
                "J.R.R. Tolkien",
                "9780007525492",
                "https://example.com/cover.jpg",
                310
        );

        Mockito.when(bookService.addBook(eq(userId), any(BookRequestDTO.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(post("/books/{userId}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
