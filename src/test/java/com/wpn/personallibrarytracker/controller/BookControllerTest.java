package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.BookRequestDTO;
import com.wpn.personallibrarytracker.dto.BookResponseDTO;
import com.wpn.personallibrarytracker.exceptions.BookNotFoundForUserException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;

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

    @Test
    void getBooksByUserId_shouldReturn200AndBooksList_whenBooksExist() throws Exception {
        // Arrange
        Integer userId = 1;
        BookResponseDTO book1 = new BookResponseDTO(
                101,
                "The Hobbit",
                "J.R.R. Tolkien",
                "9780007525492",
                "https://example.com/cover.jpg",
                310
        );
        BookResponseDTO book2 = new BookResponseDTO(
                102,
                "The Silmarillion",
                "J.R.R. Tolkien",
                "9780007525508",
                "https://example.com/cover2.jpg",
                365
        );

        Mockito.when(bookService.getBooksByUser(userId)).thenReturn(List.of(book1, book2));

        // Act & Assert
        mockMvc.perform(get("/books/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].bookId").value(101))
                .andExpect(jsonPath("$[0].title").value("The Hobbit"))
                .andExpect(jsonPath("$[1].bookId").value(102))
                .andExpect(jsonPath("$[1].title").value("The Silmarillion"));
    }

    @Test
    void getBooksByUserId_shouldReturn404_whenUserNotFound() throws Exception {
        // Arrange
        Integer userId = 999;
        Mockito.when(bookService.getBooksByUser(userId))
                .thenThrow(new UserNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/books/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookByBookdIdUserId_shouldReturn200AndBookDetailsResponseDTO_whenUserExistsAndBookExists() throws Exception {
        // Arrange
        Integer userId = 1;
        Integer bookId = 101;

        com.wpn.personallibrarytracker.dto.ReadingSessionResponseDTO readingSession = new com.wpn.personallibrarytracker.dto.ReadingSessionResponseDTO(
                1, 50, 100, java.time.LocalDateTime.now()
        );
        com.wpn.personallibrarytracker.dto.NoteResponseDTO note = new com.wpn.personallibrarytracker.dto.NoteResponseDTO(
                1, "Great book", java.time.LocalDateTime.now(), 10
        );
        com.wpn.personallibrarytracker.dto.ReviewResponseDTO review = new com.wpn.personallibrarytracker.dto.ReviewResponseDTO(
                1, "Amazing", 5
        );

        com.wpn.personallibrarytracker.dto.BookDetailsResponseDTO response = new com.wpn.personallibrarytracker.dto.BookDetailsResponseDTO(
                bookId,
                "The Hobbit",
                "J.R.R. Tolkien",
                310,
                "9780007525492",
                "https://example.com/cover.jpg",
                List.of(readingSession),
                List.of(note),
                review
        );

        Mockito.when(bookService.getBookByUser(eq(userId), eq(bookId))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/books/{userId}/{bookId}", userId, bookId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(101))
                .andExpect(jsonPath("$.title").value("The Hobbit"))
                .andExpect(jsonPath("$.author").value("J.R.R. Tolkien"))
                .andExpect(jsonPath("$.totalPages").value(310))
                .andExpect(jsonPath("$.readingSessionList.size()").value(1))
                .andExpect(jsonPath("$.notes.size()").value(1))
                .andExpect(jsonPath("$.review.rating").value(5));
    }

    @Test
    void getBookByBookIdUserId_shouldReturn404_whenUserNotFound() throws Exception {
        // Arrange
        Integer userId = 999;
        Integer bookId = 101;

        Mockito.when(bookService.getBookByUser(eq(userId), eq(bookId)))
                .thenThrow(new UserNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/books/{userId}/{bookId}", userId, bookId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookByBookIdUserId_shouldReturn404_whenBookNotFoundForUser() throws Exception {
        // Arrange
        Integer userId = 1;
        Integer bookId = 999;

        Mockito.when(bookService.getBookByUser(eq(userId), eq(bookId)))
                .thenThrow(new BookNotFoundForUserException("Book not found for user"));

        // Act & Assert
        mockMvc.perform(get("/books/{userId}/{bookId}", userId, bookId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
