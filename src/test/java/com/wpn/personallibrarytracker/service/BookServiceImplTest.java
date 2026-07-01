package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.BookRequestDTO;
import com.wpn.personallibrarytracker.dto.BookResponseDTO;
import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.Note;
import com.wpn.personallibrarytracker.entity.ReadingSession;
import com.wpn.personallibrarytracker.entity.Review;
import com.wpn.personallibrarytracker.entity.User;
import com.wpn.personallibrarytracker.exceptions.BookNotFoundForUserException;
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
import java.util.List;
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

    @Test
    public void getBooksByUser_shouldReturnBookResponseDTOList_whenUserExistsAndHasBooks() {
        // Arrange
        Integer userId = 1;
        User user = new User();
        user.setUserId(userId);

        Book book = new Book();
        book.setBookId(101);
        book.setTitle("The Hobbit");
        book.setAuthor("J.R.R. Tolkien");
        book.setIsbn("9780007525492");
        book.setCoverUrl("https://example.com/cover.jpg");
        book.setTotalPages(310);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(bookRepository.findByUserUserId(userId)).thenReturn(List.of(book));

        // Act
        List<BookResponseDTO> response = bookService.getBooksByUser(userId);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals(101, response.get(0).bookId());
        Assertions.assertEquals("The Hobbit", response.get(0).title());
    }

    @Test
    public void getBooksByUser_shouldReturnEmptyList_whenUserExistsAndHasNoBooks() {
        // Arrange
        Integer userId = 1;
        User user = new User();
        user.setUserId(userId);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(bookRepository.findByUserUserId(userId)).thenReturn(List.of());

        // Act
        List<BookResponseDTO> response = bookService.getBooksByUser(userId);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.isEmpty());
    }

    @Test
    public void getBooksByUser_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        // Arrange
        Integer userId = 999;

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());
        Mockito.when(environment.getProperty("Service.USER_NOT_FOUND")).thenReturn("User not found");

        // Act & Assert
        UserNotFoundException exception = Assertions.assertThrows(
                UserNotFoundException.class,
                () -> bookService.getBooksByUser(userId)
        );

        Assertions.assertEquals("User not found", exception.getMessage());
        Mockito.verify(bookRepository, Mockito.never()).findByUserUserId(any());
    }

    public void getBookByUser_shouldReturnBookDetailsResposeDTO_whenUserIdAndBookIdAreValid() {
        // Arrange
        Integer userId = 1;
        Integer bookId = 101;

        User user = new User();
        user.setUserId(userId);

        Book book = new Book();
        book.setBookId(bookId);
        book.setTitle("The Hobbit");
        book.setAuthor("J.R.R. Tolkien");
        book.setTotalPages(310);
        book.setIsbn("9780007525492");
        book.setCoverUrl("https://example.com/cover.jpg");

        ReadingSession session = new ReadingSession();
        session.setReadingSessionId(1);
        session.setPagesReadInSession(50);
        session.setEndSessionPageNumber(100);
        session.setSessionDateTime(java.time.LocalDateTime.of(2026, 6, 30, 10, 0));
        book.setReadingSessions(List.of(session));

        Note note = new Note();
        note.setNoteId(1);
        note.setContent("Great book");
        note.setCreatedAt(java.time.LocalDateTime.of(2026, 6, 30, 10, 0));
        note.setPageNumber(10);
        book.setNotes(List.of(note));

        Review review = new Review();
        review.setReviewId(1);
        review.setContent("Amazing");
        review.setRating(5);
        book.setReview(review);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(bookRepository.findByBookIdAndUserUserId(bookId, userId)).thenReturn(Optional.of(book));

        // Act
        com.wpn.personallibrarytracker.dto.BookDetailsResponseDTO response = bookService.getBookByUser(userId, bookId);

        // Assert
        org.junit.jupiter.api.Assertions.assertNotNull(response);
        org.junit.jupiter.api.Assertions.assertEquals(bookId, response.bookId());
        org.junit.jupiter.api.Assertions.assertEquals("The Hobbit", response.title());
        org.junit.jupiter.api.Assertions.assertEquals("J.R.R. Tolkien", response.author());
        org.junit.jupiter.api.Assertions.assertEquals(310, response.totalPages());
        org.junit.jupiter.api.Assertions.assertEquals(1, response.readingSessionList().size());
        org.junit.jupiter.api.Assertions.assertEquals(1, response.notes().size());
        org.junit.jupiter.api.Assertions.assertNotNull(response.review());
        org.junit.jupiter.api.Assertions.assertEquals(5, response.review().rating());
    }

    public void getBookByUser_shouldThrowUserNotFoundException_whenUserIdIsInvalid() {
        // Arrange
        Integer userId = 999;
        Integer bookId = 101;

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());
        Mockito.when(environment.getProperty("Service.USER_NOT_FOUND")).thenReturn("User not found");

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                UserNotFoundException.class,
                () -> bookService.getBookByUser(userId, bookId)
        );
        Mockito.verify(bookRepository, org.mockito.Mockito.never()).findByBookIdAndUserUserId(org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());
    }

    public void getBookByUser_shouldThrowBookNotFoundForUserException_whenUserIdIsValidAndBookIdIsInvalid() {
        // Arrange
        Integer userId = 1;
        Integer bookId = 999;

        User user = new User();
        user.setUserId(userId);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(bookRepository.findByBookIdAndUserUserId(bookId, userId)).thenReturn(Optional.empty());

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                BookNotFoundForUserException.class,
                () -> bookService.getBookByUser(userId, bookId)
        );
    }
}
