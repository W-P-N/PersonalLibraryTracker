package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.bookDTOs.BookDetailsResponseDTO;
import com.wpn.personallibrarytracker.dto.bookDTOs.BookRequestDTO;
import com.wpn.personallibrarytracker.dto.bookDTOs.BookResponseDTO;
import com.wpn.personallibrarytracker.dto.bookDTOs.BookUpdateRequestDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteResponseDTO;
import com.wpn.personallibrarytracker.dto.readingSessionDTOs.ReadingSessionResponseDTO;
import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewResponseDTO;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
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
        mockMvc.perform(post("/users/{userId}/books", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
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
        mockMvc.perform(post("/users/{userId}/books", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBooks_shouldReturn200AndBooksList_whenBooksExist() throws Exception {
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
        mockMvc.perform(get("/users/{userId}/books", userId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].bookId").value(101))
                .andExpect(jsonPath("$[0].title").value("The Hobbit"))
                .andExpect(jsonPath("$[1].bookId").value(102))
                .andExpect(jsonPath("$[1].title").value("The Silmarillion"));
    }

    @Test
    void getBooks_shouldReturn404_whenUserNotFound() throws Exception {
        // Arrange
        Integer userId = 999;
        Mockito.when(bookService.getBooksByUser(userId))
                .thenThrow(new UserNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/users/{userId}/books", userId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookById_shouldReturn200AndBookDetailsResponseDTO_whenUserExistsAndBookExists() throws Exception {
        // Arrange
        Integer userId = 1;
        Integer bookId = 101;

        ReadingSessionResponseDTO readingSession = new ReadingSessionResponseDTO(
                1, 50, 100, java.time.LocalDateTime.now()
        );
        NoteResponseDTO note = new NoteResponseDTO(
                1, LocalDateTime.now(), 10
        );
        ReviewResponseDTO review = new ReviewResponseDTO(
                "Amazing", 1, LocalDateTime.now()
        );

        BookDetailsResponseDTO response = new BookDetailsResponseDTO(
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

        Mockito.when(bookService.getBookDetails(eq(userId), eq(bookId))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/users/{userId}/books/{bookId}", userId, bookId)
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
    void getBookById_shouldReturn400UserNotFound_whenUserIdIsInvalid() throws Exception {
        // Arrange
        Integer userId = 999;
        Integer bookId = 101;

        Mockito.when(bookService.getBookDetails(eq(userId), eq(bookId)))
                .thenThrow(new UserNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/users/{userId}/books/{bookId}", userId, bookId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookId_shouldReturn404_whenBookNotFoundForUser() throws Exception {
        // Arrange
        Integer userId = 1;
        Integer bookId = 999;

        Mockito.when(bookService.getBookDetails(eq(userId), eq(bookId)))
                .thenThrow(new BookNotFoundForUserException("Book not found for user"));

        // Act & Assert
        mockMvc.perform(get("/users/{userId}/books/{bookId}", userId, bookId)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBook_shouldReturn200() throws Exception {
        // Arrange
        Integer mockUserId = 12;
        Integer mockBookId = 23;
        BookUpdateRequestDTO mockBookUpdateRequestDTO = new BookUpdateRequestDTO(
                "test title",
                "test author",
                123,
                "23542513",
                "https://wer.sfg.rwt"
        );

        BookResponseDTO mockBookResponseDTO = new BookResponseDTO(
                mockBookId,
                "test title",
                "test author",
                "23542513",
                "https://wer.sfg.rwt",
                123
        );
        Mockito.when(bookService.updateBook(eq(mockUserId), eq(mockBookId), eq(mockBookUpdateRequestDTO)))
                        .thenReturn(mockBookResponseDTO);
        // Act
        mockMvc.perform(patch("/users/{userId}/books/{bookId}", mockUserId, mockBookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockBookUpdateRequestDTO))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("test title"))
                .andExpect(jsonPath("$.author").value("test author"))
                .andExpect(jsonPath("$.totalPages").value(123))
                .andExpect(jsonPath("$.isbn").value("23542513"))
                .andExpect(jsonPath("$.coverUrl").value("https://wer.sfg.rwt"));

    }

    @Test
    void updateBook_shouldReturn404_whenUserNotFound() throws Exception {
        // Arrange
        Integer mockUserId = 12;
        Integer mockBookId = 23;
        BookUpdateRequestDTO mockBookUpdateRequestDTO = new BookUpdateRequestDTO(
                "test title",
                "test author",
                123,
                "23542513",
                "https://wer.sfg.rwt"
        );
        Mockito.when(bookService.updateBook(mockUserId, mockBookId, mockBookUpdateRequestDTO))
                .thenThrow(UserNotFoundException.class);
        // Act
        mockMvc.perform(patch("/users/{userId}/books/{bookId}", mockUserId, mockBookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockBookUpdateRequestDTO))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateBook_shouldReturn404_whenBookNotFound() throws Exception {
        // Arrange
        Integer mockUserId = 12;
        Integer mockBookId = 23;
        BookUpdateRequestDTO mockBookUpdateRequestDTO = new BookUpdateRequestDTO(
                "test title",
                "test author",
                123,
                "23542513",
                "https://wer.sfg.rwt"
        );
        Mockito.when(bookService.updateBook(mockUserId, mockBookId, mockBookUpdateRequestDTO))
                .thenThrow(BookNotFoundForUserException.class);
        // Act
        mockMvc.perform(patch("/users/{userId}/books/{bookId}", mockUserId, mockBookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockBookUpdateRequestDTO))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBook_shouldReturn204_whenBookIsDeletedSuccessfully() throws Exception {
        // Arrange
        Integer mockUserId = 12;
        Integer mockBookId = 23;

        Mockito.doNothing()
                .when(bookService)
                .deleteBook(mockUserId, mockBookId);

        // Act
        mockMvc.perform(delete("/users/{userId}/books/{bookId}", mockUserId, mockBookId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Assert
        Mockito.verify(bookService, Mockito.times(1))
                .deleteBook(mockUserId, mockBookId);
    }

    @Test
    void deleteBook_shouldReturn404_whenUserNotFound() throws Exception {
        // Arrange
        Integer mockUserId = 12;
        Integer mockBookId = 23;

        Mockito.doThrow(UserNotFoundException.class)
                .when(bookService)
                .deleteBook(mockUserId, mockBookId);

        // Act
        mockMvc.perform(delete("/users/{userId}/books/{bookId}", mockUserId, mockBookId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBook_shouldReturn404_whenBookNotFound() throws Exception {
        // Arrange
        Integer mockUserId = 12;
        Integer mockBookId = 23;

        Mockito.doThrow(BookNotFoundForUserException.class)
                .when(bookService)
                .deleteBook(mockUserId, mockBookId);

        // Act
        mockMvc.perform(delete("/users/{userId}/books/{bookId}", mockUserId, mockBookId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
