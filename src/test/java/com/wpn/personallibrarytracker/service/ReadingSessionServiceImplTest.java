package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.readingSessionDTOs.ReadingSessionRequestDTO;
import com.wpn.personallibrarytracker.dto.readingSessionDTOs.ReadingSessionDetailsResponseDTO;
import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.ReadingSession;
import com.wpn.personallibrarytracker.projections.ReadingSessionProjection;
import com.wpn.personallibrarytracker.entity.User;
import com.wpn.personallibrarytracker.exceptions.ReadingSessionNotFound;
import com.wpn.personallibrarytracker.repository.BookRepository;
import com.wpn.personallibrarytracker.repository.ReadingSessionRepository;
import com.wpn.personallibrarytracker.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.exceptions.BookNotFoundForUserException;
import com.wpn.personallibrarytracker.exceptions.InvalidPageNumberException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class ReadingSessionServiceImplTest {
    private ReadingSessionProjection mockProjection(Integer id, Integer pagesRead, Integer endPage, LocalDateTime dateTime) {
        return new ReadingSessionProjection() {
            @Override
            public Integer getReadingSessionId() {
                return id;
            }

            @Override
            public LocalDateTime getSessionDateTime() {
                return dateTime;
            }

            @Override
            public Integer getEndSessionPageNumber() {
                return endPage;
            }

            @Override
            public Integer getPagesReadInSession() {
                return pagesRead;
            }
        };
    }

    @Mock
    UserRepository userRepository;
    @Mock
    BookRepository bookRepository;
    @Mock
    ReadingSessionRepository readingSessionRepository;
    @InjectMocks
    ReadingSessionServiceImpl readingSessionService;
    @Mock
    Environment environment;

    @Test
    void logReadingSession_shouldReturnReadingSessionResponseDTO() {
        // Arrange
        User newUser = new User();
        newUser.setUserId(1);

        Book newBook = new Book();
        newBook.setBookId(10);
        newBook.setTotalPages(400);
        newBook.setUser(newUser);

        ReadingSession previousSession = new ReadingSession();
        previousSession.setReadingSessionId(100);
        previousSession.setBook(newBook);
        previousSession.setEndSessionPageNumber(14);
        previousSession.setSessionDateTime(LocalDateTime.now().minusDays(1));

        Mockito.when(readingSessionRepository.save(Mockito.any(ReadingSession.class)))
                .thenAnswer(invocation -> {
                    ReadingSession session = invocation.getArgument(0);
                    session.setReadingSessionId(101);
                    return session;
                });

        Mockito.lenient().when(readingSessionRepository.findSessionWithComputedPages(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(java.util.Optional.of(mockProjection(102, 106, 120, java.time.LocalDateTime.now())));
        ReadingSessionRequestDTO readingSessionRequestDTO = new ReadingSessionRequestDTO(
                120
        );



        Mockito.when(userRepository.existsById(Mockito.anyInt()))
                .thenReturn(true);
        Mockito.when(bookRepository.findByBookIdAndUserUserId(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Optional.of(newBook));
        Mockito.when(readingSessionRepository
                .findTopByBookBookIdAndBookUserUserIdOrderBySessionDateTimeDesc(
                        Mockito.anyInt(),
                        Mockito.anyInt()
                ))
                .thenReturn(Optional.of(previousSession));
        // Act
        ReadingSessionDetailsResponseDTO readingSessionDetailsResponseDTO = readingSessionService.logSession(
                newUser.getUserId(),
                newBook.getBookId(),
                readingSessionRequestDTO
        );
        // Assert
        Assertions.assertNotNull(readingSessionDetailsResponseDTO);
        Assertions.assertNotNull(readingSessionDetailsResponseDTO.readingSessionId());
        Assertions.assertEquals(106, readingSessionDetailsResponseDTO.pagesReadInSession());
        Assertions.assertEquals(120, readingSessionDetailsResponseDTO.endSessionPageNumber());

        // Verify
        Mockito.verify(userRepository, Mockito.times(1)).existsById(Mockito.anyInt());
        Mockito.verify(bookRepository, Mockito.times(1))
                .findByBookIdAndUserUserId(Mockito.anyInt(), Mockito.anyInt());
        Mockito.verify(readingSessionRepository, Mockito.times(1))
                .findTopByBookBookIdAndBookUserUserIdOrderBySessionDateTimeDesc(
                        Mockito.anyInt(),
                        Mockito.anyInt()
                );

    }

    @Test
    void logReadingSession_NoPreviousSession_shouldReturnReadingSessionResponseDTO() {
        // Arrange
        User newUser = new User();
        newUser.setUserId(1);

        Book newBook = new Book();
        newBook.setBookId(10);
        newBook.setTotalPages(400);
        newBook.setUser(newUser);

        Mockito.when(readingSessionRepository.save(Mockito.any(ReadingSession.class)))
                .thenAnswer(invocation -> {
                    ReadingSession session = invocation.getArgument(0);
                    session.setReadingSessionId(102);
                    return session;
                });

        Mockito.lenient().when(readingSessionRepository.findSessionWithComputedPages(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Optional.of(mockProjection(102, 50, 50, LocalDateTime.now())));
        ReadingSessionRequestDTO readingSessionRequestDTO = new ReadingSessionRequestDTO(
                50 // First session reading up to page 50
        );

        Mockito.when(userRepository.existsById(Mockito.anyInt())).thenReturn(true);
        Mockito.when(bookRepository.findByBookIdAndUserUserId(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Optional.of(newBook));
        Mockito.when(readingSessionRepository.findTopByBookBookIdAndBookUserUserIdOrderBySessionDateTimeDesc(
                Mockito.anyInt(), Mockito.anyInt())).thenReturn(Optional.empty());

        // Act
        ReadingSessionDetailsResponseDTO response = readingSessionService.logSession(
                newUser.getUserId(), newBook.getBookId(), readingSessionRequestDTO
        );

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(102, response.readingSessionId());
        Assertions.assertEquals(50, response.pagesReadInSession());
        Assertions.assertEquals(50, response.endSessionPageNumber());

        Mockito.verify(userRepository).existsById(newUser.getUserId());
        Mockito.verify(bookRepository).findByBookIdAndUserUserId(newBook.getBookId(), newUser.getUserId());
        Mockito.verify(readingSessionRepository).findTopByBookBookIdAndBookUserUserIdOrderBySessionDateTimeDesc(
                newBook.getBookId(), newUser.getUserId());
    }

    @Test
    void logReadingSession_UserNotFound_shouldThrowUserNotFoundException() {
        // Arrange
        ReadingSessionRequestDTO requestDTO = new ReadingSessionRequestDTO(10);
        Mockito.when(userRepository.existsById(Mockito.anyInt())).thenReturn(false);
        Mockito.when(environment.getProperty("Service.USER_NOT_FOUND")).thenReturn("User not found");

        // Act & Assert
        UserNotFoundException exception = Assertions.assertThrows(UserNotFoundException.class, () -> {
            readingSessionService.logSession(1, 10, requestDTO);
        });

        Assertions.assertEquals("User not found", exception.getMessage());
        Mockito.verify(userRepository).existsById(1);
        Mockito.verifyNoInteractions(bookRepository, readingSessionRepository);
    }

    @Test
    void logReadingSession_BookNotFound_shouldThrowBookNotFoundForUserException() {
        // Arrange
        ReadingSessionRequestDTO requestDTO = new ReadingSessionRequestDTO(10);
        Mockito.when(userRepository.existsById(Mockito.anyInt())).thenReturn(true);
        Mockito.when(bookRepository.findByBookIdAndUserUserId(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Optional.empty());
        Mockito.when(environment.getProperty("Service.BOOK_NOT_FOUND_FOR_USER")).thenReturn("Book not found");

        // Act & Assert
        BookNotFoundForUserException exception = Assertions.assertThrows(BookNotFoundForUserException.class, () -> {
            readingSessionService.logSession(1, 10, requestDTO);
        });

        Assertions.assertEquals("Book not found", exception.getMessage());
        Mockito.verify(userRepository).existsById(1);
        Mockito.verify(bookRepository).findByBookIdAndUserUserId(10, 1);
        Mockito.verifyNoInteractions(readingSessionRepository);
    }

    @Test
    void logReadingSession_InvalidPageNumberGoingBackwards_shouldThrowInvalidPageNumberException() {
        // Arrange
        User newUser = new User();
        newUser.setUserId(1);

        Book newBook = new Book();
        newBook.setBookId(10);
        newBook.setTotalPages(400);
        newBook.setUser(newUser);

        ReadingSession previousSession = new ReadingSession();
        previousSession.setEndSessionPageNumber(100);

        ReadingSessionRequestDTO requestDTO = new ReadingSessionRequestDTO(90); // Going backwards

        Mockito.when(userRepository.existsById(Mockito.anyInt())).thenReturn(true);
        Mockito.when(bookRepository.findByBookIdAndUserUserId(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Optional.of(newBook));
        Mockito.when(readingSessionRepository.findTopByBookBookIdAndBookUserUserIdOrderBySessionDateTimeDesc(
                Mockito.anyInt(), Mockito.anyInt())).thenReturn(Optional.of(previousSession));
        Mockito.when(environment.getProperty("Service.PAGE_NUMBER_GOING_BACKWARDS"))
                .thenReturn("Invalid page number backwards");

        // Act & Assert
        InvalidPageNumberException exception = Assertions.assertThrows(InvalidPageNumberException.class, () -> {
            readingSessionService.logSession(1, 10, requestDTO);
        });

        Assertions.assertEquals("Invalid page number backwards", exception.getMessage());
        Mockito.verify(readingSessionRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void logReadingSession_EndPageExceedsBook_shouldThrowInvalidPageNumberException() {
        // Arrange
        User newUser = new User();
        newUser.setUserId(1);

        Book newBook = new Book();
        newBook.setBookId(10);
        newBook.setTotalPages(200); // Book has 200 pages
        newBook.setUser(newUser);

        ReadingSessionRequestDTO requestDTO = new ReadingSessionRequestDTO(250); // Exceeds book pages

        Mockito.when(userRepository.existsById(Mockito.anyInt())).thenReturn(true);
        Mockito.when(bookRepository.findByBookIdAndUserUserId(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Optional.of(newBook));
        Mockito.when(environment.getProperty("Service.PAGE_NUMBER_EXCEEDS_BOOK"))
                .thenReturn("Exceeds book length");

        // Act & Assert
        InvalidPageNumberException exception = Assertions.assertThrows(InvalidPageNumberException.class, () -> {
            readingSessionService.logSession(1, 10, requestDTO);
        });

        Assertions.assertEquals("Exceeds book length", exception.getMessage());
        Mockito.verify(readingSessionRepository, Mockito.never()).save(Mockito.any());
    }

    // --- Tests for getSessions ---
    @Test
    void getSessions_HappyPath_shouldReturnPageOfSessions() {
        // Arrange
        User newUser = new User();
        newUser.setUserId(1);

        Book newBook = new Book();
        newBook.setBookId(10);
        newBook.setTotalPages(400);
        newBook.setUser(newUser);

        ReadingSession session = new ReadingSession();
        session.setReadingSessionId(100);
        session.setEndSessionPageNumber(50);
        session.setSessionDateTime(LocalDateTime.now());

        Pageable pageable = PageRequest.of(0, 10);
        ReadingSessionProjection sessionProjection = mockProjection(100, 50, 50, LocalDateTime.now());
        org.springframework.data.domain.Page<ReadingSessionProjection> readingSessionPage =
                new org.springframework.data.domain.PageImpl<>(List.of(sessionProjection));

        Mockito.when(userRepository.existsById(Mockito.anyInt())).thenReturn(true);
        Mockito.when(bookRepository.existsByBookIdAndUserUserId(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(true);
        Mockito.when(readingSessionRepository.findSessionsWithComputedPages(
                Mockito.anyInt(), Mockito.anyInt(), Mockito.any(Pageable.class)))
                .thenReturn(readingSessionPage);

        // Act
        org.springframework.data.domain.Page<ReadingSessionDetailsResponseDTO> result = readingSessionService.getSessions(1, 10, pageable);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(100, result.getContent().get(0).readingSessionId());
        
        Mockito.verify(userRepository).existsById(1);
        Mockito.verify(bookRepository).existsByBookIdAndUserUserId(10, 1);
        Mockito.verify(readingSessionRepository).findSessionsWithComputedPages(10, 1, pageable);
    }

    @Test
    void getSessions_UserNotFound_shouldThrowUserNotFoundException() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Mockito.when(userRepository.existsById(Mockito.anyInt())).thenReturn(false);
        Mockito.when(environment.getProperty("Service.USER_NOT_FOUND")).thenReturn("User not found");

        // Act & Assert
        UserNotFoundException exception = Assertions.assertThrows(UserNotFoundException.class, () -> {
            readingSessionService.getSessions(1, 10, pageable);
        });

        Assertions.assertEquals("User not found", exception.getMessage());
        Mockito.verify(userRepository).existsById(1);
        Mockito.verifyNoInteractions(bookRepository, readingSessionRepository);
    }

    @Test
    void getSessions_BookNotFound_shouldThrowBookNotFoundForUserException() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Mockito.when(userRepository.existsById(Mockito.anyInt())).thenReturn(true);
        Mockito.when(bookRepository.existsByBookIdAndUserUserId(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(false);
        Mockito.when(environment.getProperty("Service.BOOK_NOT_FOUND_FOR_USER")).thenReturn("Book not found");

        // Act & Assert
        BookNotFoundForUserException exception = Assertions.assertThrows(BookNotFoundForUserException.class, () -> {
            readingSessionService.getSessions(1, 10, pageable);
        });

        Assertions.assertEquals("Book not found", exception.getMessage());
        Mockito.verify(userRepository).existsById(1);
        Mockito.verify(bookRepository).existsByBookIdAndUserUserId(10, 1);
        Mockito.verifyNoInteractions(readingSessionRepository);
    }

    // --- Tests for getSessionById ---
    @Test
    void getSessionById_happyPath_shouldReturnReadingSessionResponseDTO() {
        // Arrange
        User mockUser = new User();
        mockUser.setUserId(1);

        Book mockBook = new Book();
        mockBook.setBookId(10);
        mockBook.setTotalPages(400);
        mockBook.setUser(mockUser);

        ReadingSession mockReadingSession = new ReadingSession();
        mockReadingSession.setReadingSessionId(12);
        mockReadingSession.setEndSessionPageNumber(50);
        mockReadingSession.setSessionDateTime(LocalDateTime.now());
        mockReadingSession.setBook(mockBook);

        Mockito.when(userRepository.existsById(Mockito.anyInt()))
                        .thenReturn(true);
        Mockito.when(bookRepository.existsByBookIdAndUserUserId(
                                Mockito.anyInt(), Mockito.anyInt()
                        ))
                        .thenReturn(true);
        Mockito.when(readingSessionRepository.findSessionWithComputedPages(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(java.util.Optional.of(mockProjection(12, 20, 50, java.time.LocalDateTime.now())));
        // Act
        ReadingSessionDetailsResponseDTO readingSessionDetailsResponseDTO = readingSessionService.getSessionById(
               1,
                10,
                12
        );
        // Assert
        Assertions.assertNotNull(readingSessionDetailsResponseDTO);
        Assertions.assertEquals(12, readingSessionDetailsResponseDTO.readingSessionId());
        Assertions.assertEquals(20, readingSessionDetailsResponseDTO.pagesReadInSession());
        Assertions.assertEquals(50, readingSessionDetailsResponseDTO.endSessionPageNumber());

        // Verify
        Mockito.verify(userRepository).existsById(1);
        Mockito.verify(bookRepository).existsByBookIdAndUserUserId(10, 1);
        Mockito.verify(readingSessionRepository).findSessionWithComputedPages(
                12,
                10,
                1
        );
    }

    @Test
    void getSessionById_unHappyPath_shouldThrowUserNotFoundException() {
        Integer userId = 1;
        Integer bookId = 10;
        Integer sessionId = 100;

        Mockito.when(userRepository.existsById(userId)).thenReturn(false);
        Mockito.when(environment.getProperty("Service.USER_NOT_FOUND"))
                .thenReturn("User not found");

        Assertions.assertThrows(UserNotFoundException.class,
                () -> readingSessionService.getSessionById(userId, bookId, sessionId));

        Mockito.verify(bookRepository, Mockito.never()).existsByBookIdAndUserUserId(Mockito.anyInt(), Mockito.anyInt());
        Mockito.verify(readingSessionRepository, Mockito.never())
                .findSessionWithComputedPages(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    void getSessionById_unHappyPath_shouldThrowBookNotFoundException() {
        Integer userId = 1;
        Integer bookId = 10;
        Integer sessionId = 100;

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(bookRepository.existsByBookIdAndUserUserId(bookId, userId))
                .thenReturn(false);
        Mockito.when(environment.getProperty("Service.BOOK_NOT_FOUND_FOR_USER"))
                .thenReturn("Book not found");

        Assertions.assertThrows(BookNotFoundForUserException.class,
                () -> readingSessionService.getSessionById(userId, bookId, sessionId));

        Mockito.verify(readingSessionRepository, Mockito.never())
                .findSessionWithComputedPages(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    void getSessionById_unHappyPath_shouldThrowReadingSessionNotFoundException() {
        Integer userId = 1;
        Integer bookId = 10;
        Integer sessionId = 100;

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(bookRepository.existsByBookIdAndUserUserId(bookId, userId))
                .thenReturn(true);
        Mockito.when(readingSessionRepository.findSessionWithComputedPages(
                sessionId, bookId, userId))
                .thenReturn(Optional.empty());
        Mockito.when(environment.getProperty("Service.READING_SESSION_NOT_FOUND"))
                .thenReturn("Reading session not found");

        Assertions.assertThrows(ReadingSessionNotFound.class,
                () -> readingSessionService.getSessionById(userId, bookId, sessionId));

        Mockito.verify(readingSessionRepository)
                .findSessionWithComputedPages(sessionId, bookId, userId);
    }


    // --- Tests for updateSession ---
    @Test
    void updateSession_happyPath_shouldReturnReadingSessionResponseDTO_whenNoPreviousSessionAndNextSessionsArePresent() {
        // Arrange
        User mockUser = new User();
        mockUser.setUserId(1);

        Book mockBook = new Book();
        mockBook.setBookId(10);
        mockBook.setTotalPages(400);
        mockBook.setUser(mockUser);

        ReadingSession mockReadingSession = new ReadingSession();
        mockReadingSession.setReadingSessionId(12);
        mockReadingSession.setEndSessionPageNumber(50);
        mockReadingSession.setSessionDateTime(LocalDateTime.now());
        mockReadingSession.setBook(mockBook);

        ReadingSessionRequestDTO mockReadingSessionRequestDTO =
                new ReadingSessionRequestDTO(55);

        Mockito.when(userRepository.existsById(Mockito.anyInt()))
                .thenReturn(true);
        Mockito.when(bookRepository.findByBookIdAndUserUserId(
                Mockito.anyInt(),
                Mockito.anyInt()
        ))
                .thenReturn(Optional.of(mockBook));
        Mockito.when(readingSessionRepository.findByReadingSessionIdAndBookBookIdAndBookUserUserId(
                12, 10, 1
        )).thenReturn(Optional.of(mockReadingSession));
        Mockito.when(readingSessionRepository.findSessionWithComputedPages(12, 10, 1))
                .thenReturn(Optional.of(mockProjection(12, 55, 55, mockReadingSession.getSessionDateTime())));
        Mockito.when(readingSessionRepository
                .findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeBeforeOrderBySessionDateTimeDesc(
                        Mockito.anyInt(),
                        Mockito.anyInt(),
                        Mockito.any(LocalDateTime.class)
                ))
                .thenReturn(Optional.empty());
        Mockito.when(readingSessionRepository
                .findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeAfterOrderBySessionDateTimeAsc(
                        Mockito.anyInt(),
                        Mockito.anyInt(),
                        Mockito.any(LocalDateTime.class)
                ))
                .thenReturn(Optional.empty());
        Mockito.when(
                readingSessionRepository.save(Mockito.any(ReadingSession.class))
        )
                .thenAnswer(ans -> {
                    ReadingSession savedReadingSession = ans.getArgument(0);
                    savedReadingSession.setEndSessionPageNumber(55);
                    return savedReadingSession;
                });
        // Act
        ReadingSessionDetailsResponseDTO mockReadingSessionDetailsResponseDTO = readingSessionService
                .updateSession(1, 10, 12, mockReadingSessionRequestDTO);
        // Assert
        Assertions.assertEquals(55, mockReadingSessionDetailsResponseDTO.endSessionPageNumber());
        Assertions.assertEquals(55, mockReadingSessionDetailsResponseDTO.pagesReadInSession());
        //Verify
        Mockito.verify(userRepository).existsById(1);
        Mockito.verify(bookRepository).findByBookIdAndUserUserId(10, 1);
        Mockito.verify(readingSessionRepository).findSessionWithComputedPages(
                12,
                10,
                1
        );
        Mockito.verify(readingSessionRepository).save(mockReadingSession);
    }

    @Test
    void updateSession_happyPath_shouldReturnReadingSessionResponseDTO_whenPreviousSessionFoundAndRecalculationIsValid() {
        Integer userId = 1;
        Integer bookId = 10;
        Integer sessionId = 100;

        Book foundBook = new Book();
        foundBook.setTotalPages(500);

        ReadingSession foundReadingSession = new ReadingSession();
        foundReadingSession.setReadingSessionId(sessionId);
        foundReadingSession.setSessionDateTime(LocalDateTime.of(2026, 7, 13, 10, 0));

        ReadingSession previousSession = new ReadingSession();
        previousSession.setReadingSessionId(99);
        previousSession.setEndSessionPageNumber(40);

        ReadingSessionRequestDTO requestDTO = new ReadingSessionRequestDTO(70);

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(bookRepository.findByBookIdAndUserUserId(bookId, userId))
                .thenReturn(Optional.of(foundBook));
        Mockito.when(readingSessionRepository.findByReadingSessionIdAndBookBookIdAndBookUserUserId(
                sessionId, bookId, userId))
                .thenReturn(Optional.of(foundReadingSession));
        Mockito.when(readingSessionRepository.findSessionWithComputedPages(
                sessionId, bookId, userId))
                .thenReturn(Optional.of(mockProjection(100, 30, 70, foundReadingSession.getSessionDateTime())));
        Mockito.when(readingSessionRepository
                .findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeBeforeOrderBySessionDateTimeDesc(
                        bookId, userId, foundReadingSession.getSessionDateTime()))
                .thenReturn(Optional.of(previousSession));
        Mockito.when(readingSessionRepository
                .findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeAfterOrderBySessionDateTimeAsc(
                        bookId, userId, foundReadingSession.getSessionDateTime()))
                .thenReturn(Optional.empty());
        Mockito.when(readingSessionRepository.save(Mockito.any(ReadingSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReadingSessionDetailsResponseDTO response = readingSessionService.updateSession(
                userId, bookId, sessionId, requestDTO
        );

        Assertions.assertAll(
                () -> Assertions.assertEquals(sessionId, response.readingSessionId()),
                () -> Assertions.assertEquals(30, response.pagesReadInSession()),
                () -> Assertions.assertEquals(70, response.endSessionPageNumber()),
                () -> Assertions.assertEquals(foundReadingSession.getSessionDateTime(), response.sessionDateTime())
        );

        Mockito.verify(readingSessionRepository).save(foundReadingSession);
        Mockito.verify(readingSessionRepository, Mockito.never()).save(Mockito.argThat(session -> session != foundReadingSession));
    }

    @Test
    void updateSession_happyPath_shouldReturnReadingSessionResponseDTO_whenPreviousSessionFoundAndNextSessionFoundAndRecalculationIsValid() {
        Integer userId = 1;
        Integer bookId = 10;
        Integer sessionId = 100;

        Book foundBook = new Book();
        foundBook.setTotalPages(500);

        ReadingSession foundReadingSession = new ReadingSession();
        foundReadingSession.setReadingSessionId(sessionId);
        foundReadingSession.setSessionDateTime(LocalDateTime.of(2026, 7, 13, 10, 0));

        ReadingSession previousSession = new ReadingSession();
        previousSession.setReadingSessionId(99);
        previousSession.setEndSessionPageNumber(40);

        ReadingSession nextSession = new ReadingSession();
        nextSession.setReadingSessionId(101);
        nextSession.setEndSessionPageNumber(120);

        ReadingSessionRequestDTO requestDTO = new ReadingSessionRequestDTO(70);

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(bookRepository.findByBookIdAndUserUserId(bookId, userId))
                .thenReturn(Optional.of(foundBook));
        Mockito.when(readingSessionRepository.findByReadingSessionIdAndBookBookIdAndBookUserUserId(
                sessionId, bookId, userId))
                .thenReturn(Optional.of(foundReadingSession));
        Mockito.when(readingSessionRepository.findSessionWithComputedPages(
                sessionId, bookId, userId))
                .thenReturn(Optional.of(mockProjection(100, 30, 70, foundReadingSession.getSessionDateTime())));
        Mockito.when(readingSessionRepository
                .findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeBeforeOrderBySessionDateTimeDesc(
                        bookId, userId, foundReadingSession.getSessionDateTime()))
                .thenReturn(Optional.of(previousSession));
        Mockito.when(readingSessionRepository
                .findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeAfterOrderBySessionDateTimeAsc(
                        bookId, userId, foundReadingSession.getSessionDateTime()))
                .thenReturn(Optional.of(nextSession));
        Mockito.when(readingSessionRepository.save(Mockito.any(ReadingSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReadingSessionDetailsResponseDTO response = readingSessionService.updateSession(
                userId, bookId, sessionId, requestDTO
        );

        // Assert
        Assertions.assertAll(
                () -> Assertions.assertEquals(sessionId, response.readingSessionId()),
                () -> Assertions.assertEquals(30, response.pagesReadInSession()),
                () -> Assertions.assertEquals(70, response.endSessionPageNumber()),
                () -> Assertions.assertEquals(foundReadingSession.getSessionDateTime(), response.sessionDateTime())
        );
        Mockito.verify(readingSessionRepository, Mockito.times(1)).save(foundReadingSession);
        Mockito.verify(readingSessionRepository, Mockito.never()).save(nextSession);
    }

    @Test
    void updateSession_unHappyPath_shouldThrowInvalidPageNumberExceptionPageNumberExceeds() {
        Integer userId = 1;
        Integer bookId = 10;
        Integer sessionId = 100;

        Book foundBook = new Book();
        foundBook.setTotalPages(100);

        ReadingSession foundReadingSession = new ReadingSession();
        foundReadingSession.setReadingSessionId(sessionId);
        foundReadingSession.setSessionDateTime(LocalDateTime.of(2026, 7, 13, 10, 0));

        ReadingSessionRequestDTO requestDTO = new ReadingSessionRequestDTO(150);

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(bookRepository.findByBookIdAndUserUserId(bookId, userId))
                .thenReturn(Optional.of(foundBook));
        Mockito.when(readingSessionRepository.findByReadingSessionIdAndBookBookIdAndBookUserUserId(
                sessionId, bookId, userId))
                .thenReturn(Optional.of(foundReadingSession));
        Mockito.when(environment.getProperty("Service.PAGE_NUMBER_EXCEEDS_BOOK"))
                .thenReturn("Page number exceeds book total pages");

        Assertions.assertThrows(InvalidPageNumberException.class, () ->
                readingSessionService.updateSession(userId, bookId, sessionId, requestDTO));

        Mockito.verify(readingSessionRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void updateSession_unHappyPath_shouldThrowInvalidPageNumberExceptionPageNumberGoingBackwards() {
        Integer userId = 1;
        Integer bookId = 10;
        Integer sessionId = 100;

        Book foundBook = new Book();
        foundBook.setTotalPages(500);

        ReadingSession foundReadingSession = new ReadingSession();
        foundReadingSession.setReadingSessionId(sessionId);
        foundReadingSession.setSessionDateTime(LocalDateTime.of(2026, 7, 13, 10, 0));

        ReadingSession previousSession = new ReadingSession();
        previousSession.setReadingSessionId(99);
        previousSession.setEndSessionPageNumber(80);

        ReadingSessionRequestDTO requestDTO = new ReadingSessionRequestDTO(70);

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(bookRepository.findByBookIdAndUserUserId(bookId, userId))
                .thenReturn(Optional.of(foundBook));
        Mockito.when(readingSessionRepository.findByReadingSessionIdAndBookBookIdAndBookUserUserId(
                sessionId, bookId, userId))
                .thenReturn(Optional.of(foundReadingSession));
        Mockito.when(readingSessionRepository
                .findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeBeforeOrderBySessionDateTimeDesc(
                        bookId, userId, foundReadingSession.getSessionDateTime()))
                .thenReturn(Optional.of(previousSession));
        Mockito.when(environment.getProperty("Service.PAGE_NUMBER_GOING_BACKWARDS"))
                .thenReturn("Page number going backwards");

        Assertions.assertThrows(InvalidPageNumberException.class, () ->
                readingSessionService.updateSession(userId, bookId, sessionId, requestDTO));

        Mockito.verify(readingSessionRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void updateSession_unHappyPath_shouldThrowInvalidPageNumberExceptionPageNumberGoingForwards() {
        Integer userId = 1;
        Integer bookId = 10;
        Integer sessionId = 100;

        Book foundBook = new Book();
        foundBook.setTotalPages(500);

        ReadingSession foundReadingSession = new ReadingSession();
        foundReadingSession.setReadingSessionId(sessionId);
        foundReadingSession.setSessionDateTime(LocalDateTime.of(2026, 7, 13, 10, 0));

        ReadingSession previousSession = new ReadingSession();
        previousSession.setReadingSessionId(99);
        previousSession.setEndSessionPageNumber(40);

        ReadingSession nextSession = new ReadingSession();
        nextSession.setReadingSessionId(101);
        nextSession.setEndSessionPageNumber(60);

        ReadingSessionRequestDTO requestDTO = new ReadingSessionRequestDTO(70);

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(bookRepository.findByBookIdAndUserUserId(bookId, userId))
                .thenReturn(Optional.of(foundBook));
        Mockito.when(readingSessionRepository.findByReadingSessionIdAndBookBookIdAndBookUserUserId(
                sessionId, bookId, userId))
                .thenReturn(Optional.of(foundReadingSession));
        Mockito.when(readingSessionRepository
                .findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeBeforeOrderBySessionDateTimeDesc(
                        bookId, userId, foundReadingSession.getSessionDateTime()))
                .thenReturn(Optional.of(previousSession));
        Mockito.when(readingSessionRepository
                .findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeAfterOrderBySessionDateTimeAsc(
                        bookId, userId, foundReadingSession.getSessionDateTime()))
                .thenReturn(Optional.of(nextSession));
        Mockito.when(environment.getProperty("Service.PAGE_NUMBER_GOING_FORWARDS"))
                .thenReturn("Page number going forwards");

        Assertions.assertThrows(InvalidPageNumberException.class, () ->
                readingSessionService.updateSession(userId, bookId, sessionId, requestDTO));

        Mockito.verify(readingSessionRepository, Mockito.never()).save(Mockito.any());
    }

    // --- Tests for deleteSession ---
    @Test
    void deleteSession_happyPath_shouldDeleteCurrentSession() {
        Integer userId = 1;
        Integer bookId = 10;
        Integer sessionId = 100;

        ReadingSession currentSession = new ReadingSession();
        currentSession.setReadingSessionId(sessionId);
        currentSession.setSessionDateTime(LocalDateTime.of(2026, 7, 13, 10, 0));

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(bookRepository.existsByBookIdAndUserUserId(bookId, userId)).thenReturn(true);
        Mockito.when(readingSessionRepository.findByReadingSessionIdAndBookBookIdAndBookUserUserId(
                sessionId, bookId, userId))
                .thenReturn(Optional.of(currentSession));

        Assertions.assertDoesNotThrow(() -> readingSessionService.deleteSession(userId, bookId, sessionId));

        Mockito.verify(readingSessionRepository).delete(currentSession);
        Mockito.verify(readingSessionRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void deleteSession_whenNoNextSession_shouldOnlyDeleteCurrentSession() {
        Integer userId = 1;
        Integer bookId = 10;
        Integer sessionId = 100;

        ReadingSession currentSession = new ReadingSession();
        currentSession.setReadingSessionId(sessionId);
        currentSession.setSessionDateTime(LocalDateTime.of(2026, 7, 13, 10, 0));

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(bookRepository.existsByBookIdAndUserUserId(bookId, userId)).thenReturn(true);
        Mockito.when(readingSessionRepository.findByReadingSessionIdAndBookBookIdAndBookUserUserId(
                sessionId, bookId, userId))
                .thenReturn(Optional.of(currentSession));

        Assertions.assertDoesNotThrow(() -> readingSessionService.deleteSession(userId, bookId, sessionId));

        Mockito.verify(readingSessionRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(readingSessionRepository).delete(currentSession);
    }

    @Test
    void deleteSession_whenNoPreviousSession_shouldDeleteCurrentSession() {
        Integer userId = 1;
        Integer bookId = 10;
        Integer sessionId = 100;

        ReadingSession currentSession = new ReadingSession();
        currentSession.setReadingSessionId(sessionId);
        currentSession.setSessionDateTime(LocalDateTime.of(2026, 7, 13, 10, 0));

        Mockito.when(userRepository.existsById(userId)).thenReturn(true);
        Mockito.when(bookRepository.existsByBookIdAndUserUserId(bookId, userId)).thenReturn(true);
        Mockito.when(readingSessionRepository.findByReadingSessionIdAndBookBookIdAndBookUserUserId(
                sessionId, bookId, userId))
                .thenReturn(Optional.of(currentSession));

        Assertions.assertDoesNotThrow(() -> readingSessionService.deleteSession(userId, bookId, sessionId));

        Mockito.verify(readingSessionRepository).delete(currentSession);
        Mockito.verify(readingSessionRepository, Mockito.never()).save(Mockito.any());
    }
}
