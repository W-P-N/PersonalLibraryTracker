package com.wpn.personallibrarytracker.repository;

import com.wpn.personallibrarytracker.dto.readingSessionDTOs.ReadingSessionResponseDTO;
import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.ReadingSession;
import com.wpn.personallibrarytracker.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@DataJpaTest
public class ReadingSessionRepositoryTest {
    @Autowired
    private ReadingSessionRepository readingSessionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookRepository bookRepository;

    @Test
    void findByBookBookIdAndBookUserUserId_shouldReturnReadingSessionsInPages() {
        // Arrange
        User user = new User();
        user.setUserName("testuser");
        user.setEmail("testuser@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        Book book1 = new Book();
        book1.setTitle("Title 1");
        book1.setAuthor("Author 1");
        book1.setTotalPages(100);
        book1.setUser(savedUser);
        Book savedBook = bookRepository.save(book1);

        ReadingSession readingSession1 = new ReadingSession();
        readingSession1.setBook(savedBook);
        readingSession1.setEndSessionPageNumber(12);
        readingSession1.setSessionDateTime(LocalDateTime.now());
        ReadingSession saveReadingSession1 = readingSessionRepository.save(readingSession1);

        ReadingSession readingSession2 = new ReadingSession();
        readingSession2.setBook(savedBook);
        readingSession2.setEndSessionPageNumber(24);
        readingSession2.setSessionDateTime(LocalDateTime.now());
        ReadingSession savedReadingSession2 = readingSessionRepository.save(readingSession2);

        // Act
        Pageable pageable = PageRequest.of(0, 5);
        Page<ReadingSession> readingSessionResponseDTOs = readingSessionRepository.findByBookBookIdAndBookUserUserId(savedBook.getBookId(), savedUser.getUserId(), pageable);

        // Assert
        //// Correct number of results
        Assertions.assertEquals(2, readingSessionResponseDTOs.getTotalElements());      // Current page
        Assertions.assertEquals(2, readingSessionResponseDTOs.getContent().size());        // Requested page size
        //// Returned objects are expected ones
        Assertions.assertTrue(
                readingSessionResponseDTOs.getContent().stream()
                        .allMatch(rs -> rs.getBook().getBookId().equals(savedBook.getBookId()))
        );
        Assertions.assertTrue(
                readingSessionResponseDTOs.getContent().stream()
                        .allMatch(rs -> rs.getBook().getUser().getUserId().equals(savedUser.getUserId()))
        );
        //// Correct order of elements
        List<Integer> pageNumbers = readingSessionResponseDTOs.getContent()
                .stream()
                .map(ReadingSession::getEndSessionPageNumber)
                .toList();
        Assertions.assertTrue(pageNumbers.contains(12));
        Assertions.assertTrue(pageNumbers.contains(24));
        //// Pagnation verification
        Assertions.assertEquals(0, readingSessionResponseDTOs.getNumber());       // page index
        Assertions.assertEquals(5, readingSessionResponseDTOs.getSize());         // requested page size
        Assertions.assertEquals(2, readingSessionResponseDTOs.getTotalElements());
        Assertions.assertEquals(1, readingSessionResponseDTOs.getTotalPages());
        Assertions.assertTrue(readingSessionResponseDTOs.isFirst());
        Assertions.assertTrue(readingSessionResponseDTOs.isLast());
        Assertions.assertFalse(readingSessionResponseDTOs.hasNext());
    }

    @Test
    void findByBookBookIdAndBookUserUserId_shouldReturnEmptyPageWhenNoReadingSessionsExist() {
        User user = new User();
        user.setUserName("testuser");
        user.setEmail("testuser@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        Book book = new Book();
        book.setTitle("Title 1");
        book.setAuthor("Author 1");
        book.setTotalPages(100);
        book.setUser(savedUser);
        Book savedBook = bookRepository.save(book);

        Pageable pageable = PageRequest.of(0, 5);

        Page<ReadingSession> result =
                readingSessionRepository.findByBookBookIdAndBookUserUserId(
                        savedBook.getBookId(),
                        savedUser.getUserId(),
                        pageable
                );

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertEquals(0, result.getTotalElements());
        Assertions.assertEquals(0, result.getContent().size());
    }

    @Test
    void findByBookBookIdAndBookUserUserId_shouldReturnEmptyPageWhenBookIdDoesNotMatch() {
        User user = new User();
        user.setUserName("testuser");
        user.setEmail("testuser@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        Book book1 = new Book();
        book1.setTitle("Title 1");
        book1.setAuthor("Author 1");
        book1.setTotalPages(100);
        book1.setUser(savedUser);
        Book savedBook1 = bookRepository.save(book1);

        Book book2 = new Book();
        book2.setTitle("Title 2");
        book2.setAuthor("Author 2");
        book2.setTotalPages(200);
        book2.setUser(savedUser);
        Book savedBook2 = bookRepository.save(book2);

        ReadingSession session = new ReadingSession();
        session.setBook(savedBook1);
        session.setEndSessionPageNumber(12);
        session.setSessionDateTime(LocalDateTime.now());
        readingSessionRepository.save(session);

        Pageable pageable = PageRequest.of(0, 5);

        Page<ReadingSession> result =
                readingSessionRepository.findByBookBookIdAndBookUserUserId(
                        savedBook2.getBookId(),
                        savedUser.getUserId(),
                        pageable
                );

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertEquals(0, result.getTotalElements());
    }

    @Test
    void findByBookBookIdAndBookUserUserId_shouldReturnEmptyPageWhenUserIdDoesNotMatch() {
        User user1 = new User();
        user1.setUserName("user1");
        user1.setEmail("user1@mail.com");
        user1.setPassword("password");
        User savedUser1 = userRepository.save(user1);

        User user2 = new User();
        user2.setUserName("user2");
        user2.setEmail("user2@mail.com");
        user2.setPassword("password");
        User savedUser2 = userRepository.save(user2);

        Book book = new Book();
        book.setTitle("Title 1");
        book.setAuthor("Author 1");
        book.setTotalPages(100);
        book.setUser(savedUser1);
        Book savedBook = bookRepository.save(book);

        ReadingSession session = new ReadingSession();
        session.setBook(savedBook);
        session.setEndSessionPageNumber(12);
        session.setSessionDateTime(LocalDateTime.now());
        readingSessionRepository.save(session);

        Pageable pageable = PageRequest.of(0, 5);

        Page<ReadingSession> result =
                readingSessionRepository.findByBookBookIdAndBookUserUserId(
                        savedBook.getBookId(),
                        savedUser2.getUserId(),
                        pageable
                );

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertEquals(0, result.getTotalElements());
    }

    @Test
    void findByBookBookIdAndBookUserUserId_shouldReturnEmptyPageWhenBookBelongsToDifferentUser() {
        User user1 = new User();
        user1.setUserName("user1");
        user1.setEmail("user1@mail.com");
        user1.setPassword("password");
        User savedUser1 = userRepository.save(user1);

        User user2 = new User();
        user2.setUserName("user2");
        user2.setEmail("user2@mail.com");
        user2.setPassword("password");
        User savedUser2 = userRepository.save(user2);

        Book book = new Book();
        book.setTitle("Title 1");
        book.setAuthor("Author 1");
        book.setTotalPages(100);
        book.setUser(savedUser1);
        Book savedBook = bookRepository.save(book);

        ReadingSession session = new ReadingSession();
        session.setBook(savedBook);
        session.setEndSessionPageNumber(12);
        session.setSessionDateTime(LocalDateTime.now());
        readingSessionRepository.save(session);

        Pageable pageable = PageRequest.of(0, 5);

        Page<ReadingSession> result =
                readingSessionRepository.findByBookBookIdAndBookUserUserId(
                        savedBook.getBookId(),
                        savedUser2.getUserId(),
                        pageable
                );

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertEquals(0, result.getTotalElements());
    }

    @Test
    void findByBookBookIdAndBookUserUserId_shouldReturnEmptyPageWhenPageIsOutOfRange() {
        User user = new User();
        user.setUserName("testuser");
        user.setEmail("testuser@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        Book book = new Book();
        book.setTitle("Title 1");
        book.setAuthor("Author 1");
        book.setTotalPages(100);
        book.setUser(savedUser);
        Book savedBook = bookRepository.save(book);

        ReadingSession session1 = new ReadingSession();
        session1.setBook(savedBook);
        session1.setEndSessionPageNumber(12);
        session1.setSessionDateTime(LocalDateTime.now());
        readingSessionRepository.save(session1);

        ReadingSession session2 = new ReadingSession();
        session2.setBook(savedBook);
        session2.setEndSessionPageNumber(24);
        session2.setSessionDateTime(LocalDateTime.now());
        readingSessionRepository.save(session2);

        Pageable pageable = PageRequest.of(5, 5); // out of range

        Page<ReadingSession> result =
                readingSessionRepository.findByBookBookIdAndBookUserUserId(
                        savedBook.getBookId(),
                        savedUser.getUserId(),
                        pageable
                );

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertEquals(1, result.getTotalPages()); // with page size 5 and 2 records, total pages is 1
    }

    @Test
    void findTopByBookBookIdAndBookUserUserIdOrderBySessionDateTimeDesc_shouldReturnLatestSession() {
        User user = new User();
        user.setUserName("testuser");
        user.setEmail("testuser@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        Book book = new Book();
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setTotalPages(100);
        book.setUser(savedUser);
        Book savedBook = bookRepository.save(book);

        LocalDateTime now = LocalDateTime.now();

        ReadingSession session1 = new ReadingSession();
        session1.setBook(savedBook);
        session1.setEndSessionPageNumber(10);
        session1.setSessionDateTime(now.minusDays(2));
        readingSessionRepository.save(session1);

        ReadingSession session2 = new ReadingSession();
        session2.setBook(savedBook);
        session2.setEndSessionPageNumber(20);
        session2.setSessionDateTime(now);
        ReadingSession savedSession2 = readingSessionRepository.save(session2);

        ReadingSession session3 = new ReadingSession();
        session3.setBook(savedBook);
        session3.setEndSessionPageNumber(15);
        session3.setSessionDateTime(now.minusDays(1));
        readingSessionRepository.save(session3);

        Optional<ReadingSession> result = readingSessionRepository.findTopByBookBookIdAndBookUserUserIdOrderBySessionDateTimeDesc(savedBook.getBookId(), savedUser.getUserId());

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(savedSession2.getReadingSessionId(), result.get().getReadingSessionId());
        Assertions.assertEquals(20, result.get().getEndSessionPageNumber());
    }

    @Test
    void findTopByBookBookIdAndBookUserUserIdOrderBySessionDateTimeDesc_shouldReturnEmptyWhenNoSessionsExist() {
        User user = new User();
        user.setUserName("testuser");
        user.setEmail("testuser@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        Book book = new Book();
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setTotalPages(100);
        book.setUser(savedUser);
        Book savedBook = bookRepository.save(book);

        Optional<ReadingSession> result = readingSessionRepository.findTopByBookBookIdAndBookUserUserIdOrderBySessionDateTimeDesc(savedBook.getBookId(), savedUser.getUserId());

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeAfterOrderBySessionDateTimeAsc_shouldReturnNextSession() {
        User user = new User();
        user.setUserName("testuser");
        user.setEmail("testuser@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        Book book = new Book();
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setTotalPages(100);
        book.setUser(savedUser);
        Book savedBook = bookRepository.save(book);

        LocalDateTime baseTime = LocalDateTime.now();

        ReadingSession session1 = new ReadingSession();
        session1.setBook(savedBook);
        session1.setEndSessionPageNumber(10);
        session1.setSessionDateTime(baseTime);
        readingSessionRepository.save(session1);

        ReadingSession session2 = new ReadingSession();
        session2.setBook(savedBook);
        session2.setEndSessionPageNumber(20);
        session2.setSessionDateTime(baseTime.plusDays(1));
        ReadingSession savedSession2 = readingSessionRepository.save(session2);

        ReadingSession session3 = new ReadingSession();
        session3.setBook(savedBook);
        session3.setEndSessionPageNumber(30);
        session3.setSessionDateTime(baseTime.plusDays(2));
        readingSessionRepository.save(session3);

        Optional<ReadingSession> result = readingSessionRepository.findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeAfterOrderBySessionDateTimeAsc(savedBook.getBookId(), savedUser.getUserId(), baseTime.plusHours(1));

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(savedSession2.getReadingSessionId(), result.get().getReadingSessionId());
        Assertions.assertEquals(20, result.get().getEndSessionPageNumber());
    }

    @Test
    void findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeAfterOrderBySessionDateTimeAsc_shouldReturnEmptyWhenNoSessionAfter() {
        User user = new User();
        user.setUserName("testuser");
        user.setEmail("testuser@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        Book book = new Book();
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setTotalPages(100);
        book.setUser(savedUser);
        Book savedBook = bookRepository.save(book);

        LocalDateTime baseTime = LocalDateTime.now();

        ReadingSession session1 = new ReadingSession();
        session1.setBook(savedBook);
        session1.setEndSessionPageNumber(10);
        session1.setSessionDateTime(baseTime);
        readingSessionRepository.save(session1);

        Optional<ReadingSession> result = readingSessionRepository.findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeAfterOrderBySessionDateTimeAsc(savedBook.getBookId(), savedUser.getUserId(), baseTime.plusHours(1));

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void findByReadingSessionIdAndBookBookIdAndBookUserUserId_shouldReturnSessionWhenExists() {
        User user = new User();
        user.setUserName("testuser");
        user.setEmail("testuser@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        Book book = new Book();
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setTotalPages(100);
        book.setUser(savedUser);
        Book savedBook = bookRepository.save(book);

        ReadingSession session = new ReadingSession();
        session.setBook(savedBook);
        session.setEndSessionPageNumber(10);
        session.setSessionDateTime(LocalDateTime.now());
        ReadingSession savedSession = readingSessionRepository.save(session);

        Optional<ReadingSession> result = readingSessionRepository.findByReadingSessionIdAndBookBookIdAndBookUserUserId(
                savedSession.getReadingSessionId(), savedBook.getBookId(), savedUser.getUserId()
        );

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(savedSession.getReadingSessionId(), result.get().getReadingSessionId());
    }

    @Test
    void findByReadingSessionIdAndBookBookIdAndBookUserUserId_shouldReturnEmptyWhenDoesNotExist() {
        User user = new User();
        user.setUserName("testuser");
        user.setEmail("testuser@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        Book book = new Book();
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setTotalPages(100);
        book.setUser(savedUser);
        Book savedBook = bookRepository.save(book);

        Optional<ReadingSession> result = readingSessionRepository.findByReadingSessionIdAndBookBookIdAndBookUserUserId(
                999, savedBook.getBookId(), savedUser.getUserId()
        );

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeBeforeOrderBySessionDateTimeDesc_shouldReturnPreviousSession() {
        User user = new User();
        user.setUserName("testuser");
        user.setEmail("testuser@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        Book book = new Book();
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setTotalPages(100);
        book.setUser(savedUser);
        Book savedBook = bookRepository.save(book);

        LocalDateTime baseTime = LocalDateTime.now();

        ReadingSession session1 = new ReadingSession();
        session1.setBook(savedBook);
        session1.setEndSessionPageNumber(10);
        session1.setSessionDateTime(baseTime.minusDays(2));
        readingSessionRepository.save(session1);

        ReadingSession session2 = new ReadingSession();
        session2.setBook(savedBook);
        session2.setEndSessionPageNumber(20);
        session2.setSessionDateTime(baseTime.minusDays(1));
        ReadingSession savedSession2 = readingSessionRepository.save(session2);

        ReadingSession session3 = new ReadingSession();
        session3.setBook(savedBook);
        session3.setEndSessionPageNumber(30);
        session3.setSessionDateTime(baseTime);
        readingSessionRepository.save(session3);

        Optional<ReadingSession> result = readingSessionRepository.findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeBeforeOrderBySessionDateTimeDesc(
                savedBook.getBookId(), savedUser.getUserId(), baseTime.minusHours(1));

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(savedSession2.getReadingSessionId(), result.get().getReadingSessionId());
        Assertions.assertEquals(20, result.get().getEndSessionPageNumber());
    }

    @Test
    void findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeBeforeOrderBySessionDateTimeDesc_shouldReturnEmptyWhenNoSessionBefore() {
        User user = new User();
        user.setUserName("testuser");
        user.setEmail("testuser@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        Book book = new Book();
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setTotalPages(100);
        book.setUser(savedUser);
        Book savedBook = bookRepository.save(book);

        LocalDateTime baseTime = LocalDateTime.now();

        ReadingSession session1 = new ReadingSession();
        session1.setBook(savedBook);
        session1.setEndSessionPageNumber(10);
        session1.setSessionDateTime(baseTime);
        readingSessionRepository.save(session1);

        Optional<ReadingSession> result = readingSessionRepository.findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeBeforeOrderBySessionDateTimeDesc(
                savedBook.getBookId(), savedUser.getUserId(), baseTime.minusHours(1));

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void findAllByBookUserUserIdOrderBySessionDateTimeDesc_shouldReturnSessionsInDescendingOrder() {
        // Arrange
        User user = new User();
        user.setUserName("testuser8");
        user.setEmail("testuser8@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        Book book1 = new Book();
        book1.setTitle("Title 1");
        book1.setAuthor("Author 1");
        book1.setTotalPages(100);
        book1.setUser(savedUser);
        Book savedBook1 = bookRepository.save(book1);

        Book book2 = new Book();
        book2.setTitle("Title 2");
        book2.setAuthor("Author 2");
        book2.setTotalPages(200);
        book2.setUser(savedUser);
        Book savedBook2 = bookRepository.save(book2);

        LocalDateTime now = LocalDateTime.now();

        ReadingSession session1 = new ReadingSession();
        session1.setBook(savedBook1);
        session1.setEndSessionPageNumber(10);
        session1.setSessionDateTime(now.minusDays(2));
        ReadingSession savedSession1 = readingSessionRepository.save(session1);

        ReadingSession session2 = new ReadingSession();
        session2.setBook(savedBook2);
        session2.setEndSessionPageNumber(20);
        session2.setSessionDateTime(now);
        ReadingSession savedSession2 = readingSessionRepository.save(session2);

        ReadingSession session3 = new ReadingSession();
        session3.setBook(savedBook1);
        session3.setEndSessionPageNumber(30);
        session3.setSessionDateTime(now.minusDays(1));
        ReadingSession savedSession3 = readingSessionRepository.save(session3);

        // Act
        List<ReadingSession> results = readingSessionRepository.findAllByBookUserUserIdOrderBySessionDateTimeDesc(savedUser.getUserId());

        // Assert
        Assertions.assertEquals(3, results.size());
        Assertions.assertEquals(savedSession2.getReadingSessionId(), results.get(0).getReadingSessionId());
        Assertions.assertEquals(savedSession3.getReadingSessionId(), results.get(1).getReadingSessionId());
        Assertions.assertEquals(savedSession1.getReadingSessionId(), results.get(2).getReadingSessionId());
    }

    @Test
    void findAllByBookUserUserIdOrderBySessionDateTimeDesc_shouldReturnEmptyListWhenUserHasNoSessions() {
        // Arrange
        User user = new User();
        user.setUserName("testuser9");
        user.setEmail("testuser9@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        // Act
        List<ReadingSession> results = readingSessionRepository.findAllByBookUserUserIdOrderBySessionDateTimeDesc(savedUser.getUserId());

        // Assert
        Assertions.assertTrue(results.isEmpty());
    }
}
