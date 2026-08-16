package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.bookDTOs.BookDetailsResponseDTO;
import com.wpn.personallibrarytracker.dto.bookDTOs.BookFromSearchRequestDTO;
import com.wpn.personallibrarytracker.dto.bookDTOs.BookRequestDTO;
import com.wpn.personallibrarytracker.dto.bookDTOs.BookResponseDTO;
import com.wpn.personallibrarytracker.dto.bookDTOs.BookUpdateRequestDTO;
import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.Note;
import com.wpn.personallibrarytracker.entity.ReadingSession;
import com.wpn.personallibrarytracker.entity.Review;
import com.wpn.personallibrarytracker.entity.User;
import com.wpn.personallibrarytracker.exceptions.ResourceNotFoundException;
import com.wpn.personallibrarytracker.repository.BookRepository;
import com.wpn.personallibrarytracker.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
        Integer userId = 1;
        BookRequestDTO requestDTO = new BookRequestDTO(
                "The Hobbit",
                "J.R.R. Tolkien",
                310,
                "9780007525492",
                "https://example.com/cover.jpg"
        );

        User user = new User();
        user.setUserId(userId);
        user.setUserName("john_doe");
        user.setEmail("john@example.com");

        Book savedBook = new Book();
        savedBook.setBookId(101);
        savedBook.setTitle(requestDTO.title());
        savedBook.setAuthor(requestDTO.author());
        savedBook.setIsbn(requestDTO.isbn());
        savedBook.setCoverUrl(requestDTO.coverUrl());
        savedBook.setTotalPages(requestDTO.totalPages());

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        BookResponseDTO response = bookService.addBook(userId, requestDTO);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(101, response.bookId());
        Assertions.assertEquals("The Hobbit", response.title());
        Assertions.assertEquals("J.R.R. Tolkien", response.author());
        Assertions.assertEquals("9780007525492", response.isbn());
        Assertions.assertEquals("https://example.com/cover.jpg", response.coverUrl());
        Assertions.assertEquals(310, response.totalPages());

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        Mockito.verify(bookRepository).save(bookCaptor.capture());
        Assertions.assertEquals(user, bookCaptor.getValue().getUser());
    }

    @Test
    public void addBook_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        Integer userId = 999;
        BookRequestDTO requestDTO = new BookRequestDTO(
                "The Hobbit",
                "J.R.R. Tolkien",
                310,
                "9780007525492",
                "https://example.com/cover.jpg"
        );

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());
        Mockito.when(environment.getProperty("Service.RESOURCE_NOT_FOUND"))
                .thenReturn("The requested resource was not found");

        ResourceNotFoundException exception = Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> bookService.addBook(userId, requestDTO)
        );

        Assertions.assertEquals("The requested resource was not found", exception.getMessage());
        Mockito.verify(bookRepository, Mockito.never()).save(any(Book.class));
    }

    @Test
    public void getBooksByUser_shouldReturnBookResponseDTOList_whenUserExistsAndHasBooks() {
        Integer userId = 1;

        Book book = new Book();
        book.setBookId(101);
        book.setTitle("The Hobbit");
        book.setAuthor("J.R.R. Tolkien");
        book.setIsbn("9780007525492");
        book.setCoverUrl("https://example.com/cover.jpg");
        book.setTotalPages(310);

        Pageable pageable = PageRequest.of(0, 5);
        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(bookRepository.findByUserUserId(userId, pageable)).thenReturn(new PageImpl<>(List.of(book)));

        Page<BookResponseDTO> response = bookService.getBooksByUser(userId, pageable);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(1, response.getContent().size());
        Assertions.assertEquals(101, response.getContent().get(0).bookId());
        Assertions.assertEquals("The Hobbit", response.getContent().get(0).title());
    }

    @Test
    public void getBooksByUser_shouldReturnEmptyList_whenUserExistsAndHasNoBooks() {
        Integer userId = 1;
        Pageable pageable = PageRequest.of(0, 5);

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(bookRepository.findByUserUserId(userId, pageable)).thenReturn(Page.empty());

        Page<BookResponseDTO> response = bookService.getBooksByUser(userId, pageable);

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.isEmpty());
    }

    @Test
    public void getBooksByUser_shouldThrowResourceNotFoundException_whenUserDoesNotExist() {
        Integer userId = 999;
        Pageable pageable = PageRequest.of(0, 5);

        Mockito.when(userRepository.existsById(userId)).thenReturn(false);
        Mockito.when(environment.getProperty("Service.RESOURCE_NOT_FOUND"))
                .thenReturn("The requested resource was not found");

        ResourceNotFoundException exception = Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> bookService.getBooksByUser(userId, pageable)
        );

        Assertions.assertEquals("The requested resource was not found", exception.getMessage());
        Mockito.verify(bookRepository, Mockito.never()).findByUserUserId(any(), any());
    }

    @Test
    public void addBookFromSearch_shouldSaveBookAndAssociateWithUser_whenUserExists() {
        Integer userId = 1;
        BookFromSearchRequestDTO requestDTO = new BookFromSearchRequestDTO(
                "The Hobbit",
                "J.R.R. Tolkien",
                310,
                "9780007525492",
                "https://example.com/cover.jpg"
        );

        User user = new User();
        user.setUserId(userId);
        user.setUserName("john_doe");
        user.setEmail("john@example.com");

        Book savedBook = new Book();
        savedBook.setBookId(101);
        savedBook.setTitle(requestDTO.title());
        savedBook.setAuthor(requestDTO.author());
        savedBook.setIsbn(requestDTO.isbn());
        savedBook.setCoverUrl(requestDTO.coverUrl());
        savedBook.setTotalPages(requestDTO.totalPages());

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        BookResponseDTO response = bookService.addBookFromSearch(userId, requestDTO);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(101, response.bookId());
        Assertions.assertEquals("The Hobbit", response.title());
        Assertions.assertEquals("J.R.R. Tolkien", response.author());
        Assertions.assertEquals("9780007525492", response.isbn());
        Assertions.assertEquals("https://example.com/cover.jpg", response.coverUrl());
        Assertions.assertEquals(310, response.totalPages());

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        Mockito.verify(bookRepository).save(bookCaptor.capture());
        Assertions.assertEquals(user, bookCaptor.getValue().getUser());
    }

    @Test
    public void getBookDetails_shouldReturnBookDetailsResponseDTO_whenUserIdAndBookIdAreValid() {
        Integer userId = 1;
        Integer bookId = 101;

        Book book = new Book();
        book.setBookId(bookId);
        book.setTitle("The Hobbit");
        book.setAuthor("J.R.R. Tolkien");
        book.setTotalPages(310);
        book.setIsbn("9780007525492");
        book.setCoverUrl("https://example.com/cover.jpg");

        ReadingSession session = new ReadingSession();
        session.setReadingSessionId(1);
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
        review.setCreatedAt(java.time.LocalDateTime.of(2026, 6, 30, 10, 0));
        book.setReview(review);

        Mockito.when(bookRepository.findByBookIdAndUserUserId(bookId, userId)).thenReturn(Optional.of(book));

        BookDetailsResponseDTO response = bookService.getBookDetails(userId, bookId);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(bookId, response.bookId());
        Assertions.assertEquals("The Hobbit", response.title());
        Assertions.assertEquals("J.R.R. Tolkien", response.author());
        Assertions.assertEquals(310, response.totalPages());
        Assertions.assertEquals(1, response.readingSessionList().size());
        Assertions.assertEquals(1, response.notes().size());
        Assertions.assertNotNull(response.review());
        Assertions.assertEquals(5, response.review().rating());
        Mockito.verify(userRepository, Mockito.never()).existsById(any());
    }

    @Test
    public void getBookDetails_shouldThrowResourceNotFoundException_whenBookNotFoundForUser() {
        Integer userId = 1;
        Integer bookId = 999;

        Mockito.when(bookRepository.findByBookIdAndUserUserId(bookId, userId)).thenReturn(Optional.empty());
        Mockito.when(environment.getProperty("Service.RESOURCE_NOT_FOUND"))
                .thenReturn("The requested resource was not found");

        ResourceNotFoundException exception = Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> bookService.getBookDetails(userId, bookId)
        );

        Assertions.assertEquals("The requested resource was not found", exception.getMessage());
        Mockito.verify(userRepository, Mockito.never()).existsById(any());
    }

    @Test
    public void updateBook_shouldReturnBookResponseDTO() {
        Integer userId = 12;
        Integer bookId = 14;

        Book foundBook = new Book();
        foundBook.setBookId(14);
        foundBook.setTitle("TestBook");
        foundBook.setAuthor("Test Author");
        foundBook.setCoverUrl("https://test");
        foundBook.setIsbn("12334");
        foundBook.setTotalPages(341);

        Mockito.when(bookRepository.findByBookIdAndUserUserId(bookId, userId))
                .thenReturn(Optional.of(foundBook));

        BookUpdateRequestDTO bookUpdateRequestDTO = new BookUpdateRequestDTO(
                "TestBook1",
                "Test Author 1",
                234,
                "12353",
                "https://test12"
        );

        BookResponseDTO bookResponseDTO = bookService.updateBook(userId, bookId, bookUpdateRequestDTO);

        Assertions.assertEquals(bookUpdateRequestDTO.title(), bookResponseDTO.title());
        Assertions.assertEquals(bookUpdateRequestDTO.author(), bookResponseDTO.author());
        Assertions.assertEquals(bookUpdateRequestDTO.totalPages(), bookResponseDTO.totalPages());
        Assertions.assertEquals(bookUpdateRequestDTO.isbn(), bookResponseDTO.isbn());
        Assertions.assertEquals(bookUpdateRequestDTO.coverUrl(), bookResponseDTO.coverUrl());

        Mockito.verify(bookRepository, Mockito.times(1)).save(Mockito.any(Book.class));
        Mockito.verify(userRepository, Mockito.never()).existsById(any());
    }

    @Test
    void updateBook_shouldThrowResourceNotFoundException_whenBookNotFoundForUser() {
        Integer mockUserId = 12;
        Integer mockBookId = 23;
        BookUpdateRequestDTO bookUpdateRequestDTO = new BookUpdateRequestDTO(
                "TestBook1",
                "Test Author 1",
                234,
                "12353",
                "https://test12"
        );

        Mockito.when(bookRepository.findByBookIdAndUserUserId(mockBookId, mockUserId))
                .thenReturn(Optional.empty());
        Mockito.when(environment.getProperty("Service.RESOURCE_NOT_FOUND"))
                .thenReturn("The requested resource was not found");

        Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> bookService.updateBook(mockUserId, mockBookId, bookUpdateRequestDTO)
        );

        Mockito.verify(bookRepository, Mockito.times(1))
                .findByBookIdAndUserUserId(mockBookId, mockUserId);
        Mockito.verify(userRepository, Mockito.never()).existsById(any());
    }

    @Test
    void deleteBook_shouldDeleteBook_whenBookExistsForUser() {
        Integer mockUserId = 12;
        Integer mockBookId = 23;

        Book mockBook = new Book();
        mockBook.setBookId(mockBookId);

        Mockito.when(bookRepository.findByBookIdAndUserUserId(mockBookId, mockUserId))
                .thenReturn(Optional.of(mockBook));

        bookService.deleteBook(mockUserId, mockBookId);

        Mockito.verify(bookRepository, Mockito.times(1))
                .findByBookIdAndUserUserId(mockBookId, mockUserId);
        Mockito.verify(bookRepository, Mockito.times(1)).delete(mockBook);
        Mockito.verify(userRepository, Mockito.never()).existsById(any());
    }

    @Test
    void deleteBook_shouldThrowResourceNotFoundException_whenBookNotFoundForUser() {
        Integer mockUserId = 12;
        Integer mockBookId = 23;

        Mockito.when(bookRepository.findByBookIdAndUserUserId(mockBookId, mockUserId))
                .thenReturn(Optional.empty());
        Mockito.when(environment.getProperty("Service.RESOURCE_NOT_FOUND"))
                .thenReturn("The requested resource was not found");

        Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> bookService.deleteBook(mockUserId, mockBookId)
        );

        Mockito.verify(bookRepository, Mockito.times(1))
                .findByBookIdAndUserUserId(mockBookId, mockUserId);
        Mockito.verify(bookRepository, Mockito.never()).delete(Mockito.any(Book.class));
        Mockito.verify(userRepository, Mockito.never()).existsById(any());
    }
}
