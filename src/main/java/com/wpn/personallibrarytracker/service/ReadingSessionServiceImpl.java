package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.readingSessionDTOs.ReadingSessionRequestDTO;
import com.wpn.personallibrarytracker.dto.readingSessionDTOs.ReadingSessionResponseDTO;
import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.ReadingSession;
import com.wpn.personallibrarytracker.entity.User;
import com.wpn.personallibrarytracker.exceptions.BookNotFoundForUserException;
import com.wpn.personallibrarytracker.exceptions.InvalidPageNumberException;
import com.wpn.personallibrarytracker.exceptions.ReadingSessionNotFound;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.repository.BookRepository;
import com.wpn.personallibrarytracker.repository.ReadingSessionRepository;
import com.wpn.personallibrarytracker.repository.UserRepository;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service("readingSessionService")
public class ReadingSessionServiceImpl implements ReadingSessionService{
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReadingSessionRepository readingSessionRepository;
    private final Environment environment;

    public ReadingSessionServiceImpl(
            UserRepository userRepository,
            BookRepository bookRepository,
            ReadingSessionRepository readingSessionRepository,
            Environment environment
    ) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.readingSessionRepository = readingSessionRepository;
        this.environment = environment;
    }

    @Override
    public ReadingSessionResponseDTO logSession(
            Integer userId,
            Integer bookId,
            ReadingSessionRequestDTO readingSessionRequestDTO
    ) {
        // Check if user exists
        validateUserExists(userId);
        // Check if book exists
        Book foundBook = getBookByUser(bookId, userId);
        // Validations
        // If request DTO end session is greater than total page number in the book
        if(readingSessionRequestDTO.endSessionPageNumber() > foundBook.getTotalPages()) {
            throw new InvalidPageNumberException(
                    environment.getProperty("Service.PAGE_NUMBER_EXCEEDS_BOOK")
            );
        }
        // Previous session validation -
        // The end page of request DTO should be greater than end page of previous session.
        Optional<ReadingSession> previousSession = readingSessionRepository
                .findTopByBookBookIdAndBookUserUserIdOrderBySessionDateTimeDesc(bookId, userId);
        int pagesRead = previousSession
                .map(
                        session ->
                                readingSessionRequestDTO.endSessionPageNumber() - session.getEndSessionPageNumber())
                .orElse(readingSessionRequestDTO.endSessionPageNumber());
        if(pagesRead < 1) {
            throw new InvalidPageNumberException(
                    environment.getProperty("Service.PAGE_NUMBER_GOING_BACKWARDS")
            );
        }
        // Adding new session
        ReadingSession newReadingSession = new ReadingSession();
        newReadingSession.setSessionDateTime(LocalDateTime.now());
        newReadingSession.setBook(foundBook);
        newReadingSession.setEndSessionPageNumber(readingSessionRequestDTO.endSessionPageNumber());
        newReadingSession.setPagesReadInSession(pagesRead);
        ReadingSession savedReadingSession = readingSessionRepository.save(newReadingSession);
        return new ReadingSessionResponseDTO(
                savedReadingSession.getReadingSessionId(),
                savedReadingSession.getPagesReadInSession(),
                savedReadingSession.getEndSessionPageNumber(),
                savedReadingSession.getSessionDateTime()
        );
    }

    @Override
    public Page<ReadingSessionResponseDTO> getSessions(Integer userId, Integer bookId, Pageable pageable) {
        // Check if user exists
        validateUserExists(userId);
        // Check if book exists
        validateBookByUserExists(bookId, userId);
        // Check if reading session exists
        Page<ReadingSession> readingSessionPage = readingSessionRepository
                .findByBookBookIdAndBookUserUserId(bookId, userId, pageable);
        // Send the found reading session mapped to ReadingSessionResponseDTO
        return readingSessionPage.map(session -> new ReadingSessionResponseDTO(
                session.getReadingSessionId(),
                session.getPagesReadInSession(),
                session.getEndSessionPageNumber(),
                session.getSessionDateTime()
        ));
    }

    @Override
    public ReadingSessionResponseDTO getSessionById(Integer userId, Integer bookId, Integer sessionId) {
        // Check if user exists
        validateUserExists(userId);
        // Check if book exists
        validateBookByUserExists(bookId,userId);
        // Check if reading session exists
        ReadingSession foundReadingSession = getReadingSession(
                sessionId,
                bookId,
                userId
        );
        // Return response DTO for found reading session.
        return new ReadingSessionResponseDTO(
                foundReadingSession.getReadingSessionId(),
                foundReadingSession.getPagesReadInSession(),
                foundReadingSession.getEndSessionPageNumber(),
                foundReadingSession.getSessionDateTime()
        );
    }

    @Override
    public ReadingSessionResponseDTO updateSession(
            Integer userId,
            Integer bookId,
            Integer sessionId,
            ReadingSessionRequestDTO readingSessionRequestDTO
    ) {
        validateUserExists(userId);
        Book foundBook = getBookByUser(bookId, userId);
        ReadingSession foundReadingSession = getReadingSession(
                sessionId,
                bookId,
                userId
        );
        // Validations
        if(readingSessionRequestDTO.endSessionPageNumber() > foundBook.getTotalPages()) {
            throw new InvalidPageNumberException(
                    environment.getProperty("Service.PAGE_NUMBER_EXCEEDS_BOOK")
            );
        }
        // Previous session validation
        Optional<ReadingSession> previousSession = findPreviousReadingSession(
                bookId,
                userId,
                foundReadingSession.getSessionDateTime()
        );
        int pagesRead = previousSession
                .map(
                        session ->
                                readingSessionRequestDTO.endSessionPageNumber() - session.getEndSessionPageNumber())
                .orElse(readingSessionRequestDTO.endSessionPageNumber());
        if(pagesRead < 1) {
            throw new InvalidPageNumberException(
                    environment.getProperty("Service.PAGE_NUMBER_GOING_BACKWARDS")
            );
        }
        foundReadingSession.setPagesReadInSession(pagesRead);
        // Next session validation
        Optional<ReadingSession> nextSessionOptional = findNextReadingSession(
                bookId,
                userId,
                foundReadingSession.getSessionDateTime()
        );
        if(nextSessionOptional.isPresent()) {
            ReadingSession nextSession = nextSessionOptional.get();
            if(readingSessionRequestDTO.endSessionPageNumber() > nextSession.getEndSessionPageNumber()) {
                throw new InvalidPageNumberException(
                        environment.getProperty("Service.PAGE_NUMBER_GOING_FORWARDS")
                );
            }
            nextSession.setPagesReadInSession(
                    nextSession.getEndSessionPageNumber() - readingSessionRequestDTO.endSessionPageNumber()
            );
            readingSessionRepository.save(nextSession);
        }
        // Updating:
        foundReadingSession.setEndSessionPageNumber(
                readingSessionRequestDTO.endSessionPageNumber()
        );
        ReadingSession savedReadingSession = readingSessionRepository.save(foundReadingSession);
        return new ReadingSessionResponseDTO(
                savedReadingSession.getReadingSessionId(),
                savedReadingSession.getPagesReadInSession(),
                savedReadingSession.getEndSessionPageNumber(),
                savedReadingSession.getSessionDateTime()
        );
    }

    @Override
    public void deleteSession(Integer userId, Integer bookId, Integer sessionId) {
        validateUserExists(userId);
        validateBookByUserExists(bookId,userId);
        ReadingSession foundReadingSession = readingSessionRepository
                .findByReadingSessionIdAndBookBookIdAndBookUserUserId(sessionId, bookId, userId)
                .orElseThrow(() -> new ReadingSessionNotFound(
                        environment.getProperty("Service.READING_SESSION_NOT_FOUND")
                ));
        // Update next session
        Optional<ReadingSession> nextReadingSessionOptional = findNextReadingSession(
                bookId,userId,foundReadingSession.getSessionDateTime()
        );
        if(nextReadingSessionOptional.isPresent()) {
            ReadingSession nextReadingSession = nextReadingSessionOptional.get();
            // Find previous session
            Optional<ReadingSession> previousReadingSessionOptional = findPreviousReadingSession(
                    bookId,userId,foundReadingSession.getSessionDateTime()
            );
            int recalculatedPagesRead = previousReadingSessionOptional
                    .map(prev ->
                            nextReadingSession.getEndSessionPageNumber() -
                            prev.getEndSessionPageNumber())
                    .orElse(nextReadingSession.getEndSessionPageNumber());
            nextReadingSession.setPagesReadInSession(
                    recalculatedPagesRead
            );
            readingSessionRepository.save(nextReadingSession);
        }
        readingSessionRepository.delete(foundReadingSession);
    }

    // Utility methods
    void validateUserExists(Integer userId) {
        if(!userRepository.existsById(userId)) {
            throw new UserNotFoundException(
                    environment.getProperty("Service.USER_NOT_FOUND")
            );
        };
    };

    void validateBookByUserExists(Integer bookId, Integer userId) {
        if(!bookRepository.existsByBookIdAndUserUserId(bookId, userId)) {
            throw new BookNotFoundForUserException(
                    environment.getProperty("Service.BOOK_NOT_FOUND_FOR_USER")
            );
        };
    };

    Book getBookByUser(Integer bookId, Integer userId) {
        return bookRepository.findByBookIdAndUserUserId(bookId, userId)
            .orElseThrow(() -> new BookNotFoundForUserException(
                    environment.getProperty("Service.BOOK_NOT_FOUND_FOR_USER")
            ));
    };

    Optional<ReadingSession> findNextReadingSession(
            Integer bookId, Integer userId, LocalDateTime inputDateTime
    ) {
        return readingSessionRepository
            .findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeAfterOrderBySessionDateTimeAsc(
                    bookId,
                    userId,
                    inputDateTime
            );
    };

    Optional<ReadingSession> findPreviousReadingSession(
            Integer bookId, Integer userId, LocalDateTime inputDateTime
    ) {
        return readingSessionRepository
                .findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeBeforeOrderBySessionDateTimeDesc(
                        bookId,
                        userId,
                        inputDateTime
                );
    };

    ReadingSession getReadingSession(Integer sessionId, Integer bookId, Integer userId) {
        return readingSessionRepository
                .findByReadingSessionIdAndBookBookIdAndBookUserUserId(sessionId, bookId, userId)
                .orElseThrow(() -> new ReadingSessionNotFound(
                        environment.getProperty("Service.READING_SESSION_NOT_FOUND")
                ));
    }
}
