package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.BookRequestDTO;
import com.wpn.personallibrarytracker.dto.BookResponseDTO;
import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.User;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.repository.BookRepository;
import com.wpn.personallibrarytracker.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private Environment environment;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    public void addBook_shouldSaveBookAndAssociateWithUser_whenUserExists() {
        // Arrange
        Integer userId = 1;
        BookRequestDTO requestDTO = new BookRequestDTO(
                "The Hobbit",
                "J.R.R. Tolkien",
                "9780007525492",
                "https://example.com/cover.jpg",
                310
        );

        User user = new User();
        user.setUserId(userId);
        user.setUserName("john_doe");
        user.setEmail("john@example.com");
        user.setBooks(new ArrayList<>());

        Book savedBook = new Book();
        savedBook.setBookId(101);
        savedBook.setTitle(requestDTO.title());
        savedBook.setAuthor(requestDTO.author());
        savedBook.setIsbn(requestDTO.isbn());
        savedBook.setCoverUrl(requestDTO.coverUrl());
        savedBook.setTotalPages(requestDTO.totalPages());

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        // Act
        BookResponseDTO response = bookService.addBook(userId, requestDTO);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(101, response.bookId());
        Assertions.assertEquals("The Hobbit", response.title());
        Assertions.assertEquals("J.R.R. Tolkien", response.author());
        Assertions.assertEquals("9780007525492", response.isbn());
        Assertions.assertEquals("https://example.com/cover.jpg", response.coverUrl());
        Assertions.assertEquals(310, response.totalPages());

        Assertions.assertEquals(1, user.getBooks().size());
        Assertions.assertEquals(savedBook, user.getBooks().get(0));
    }

    @Test
    public void addBook_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        // Arrange
        Integer userId = 999;
        BookRequestDTO requestDTO = new BookRequestDTO(
                "The Hobbit",
                "J.R.R. Tolkien",
                "9780007525492",
                "https://example.com/cover.jpg",
                310
        );

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());
        Mockito.when(environment.getProperty("Service.USER_NOT_FOUND")).thenReturn("User not found");

        // Act & Assert
        UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> bookService.addBook(userId, requestDTO)
        );

        Assertions.assertEquals("User not found", exception.getMessage());
        Mockito.verify(bookRepository, Mockito.never()).save(any(Book.class));
    }
}
