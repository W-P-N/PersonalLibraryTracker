package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.statsDTOs.StatsResponseDTO;
import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.ReadingSession;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.repository.BookRepository;
import com.wpn.personallibrarytracker.repository.ReadingSessionRepository;
import com.wpn.personallibrarytracker.repository.ReviewRepository;
import com.wpn.personallibrarytracker.repository.UserRepository;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("statsService")
public class StatsServiceImpl implements StatsService{
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReadingSessionRepository readingSessionRepository;
    private final ReviewRepository reviewRepository;
    private final Environment environment;

    public StatsServiceImpl(
            UserRepository userRepository,
            BookRepository bookRepository,
            ReadingSessionRepository readingSessionRepository,
            ReviewRepository reviewRepository,
            Environment environment
    ) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.readingSessionRepository = readingSessionRepository;
        this.reviewRepository = reviewRepository;
        this.environment = environment;
    }

    @Override
    @Transactional(readOnly = true)
    public StatsResponseDTO getStats(Integer userId) {
        validateUserExists(userId);
        Long totalBooks = bookRepository.countByUserUserId(userId);
        // Get list of reading sessions of the user
        List<ReadingSession> readingSessionList = readingSessionRepository
                .findAllByBookUserUserIdOrderBySessionDateTimeDesc(userId);
        // Group reading sessions by book
        Map<Book, List<ReadingSession>> sessionsByBook = readingSessionList.stream()
                .collect(Collectors.groupingBy(ReadingSession::getBook));
        // Books not started = totalBooks - sessionsByBook.size()
        Long booksNotStarted = totalBooks - sessionsByBook.size();
        // booksReading and booksFinished
        Long booksFinished = sessionsByBook.entrySet().stream()
                .map(entry -> {
                    Book book = entry.getKey();
                    ReadingSession latestReadingSession = entry.getValue().get(0);
                    return latestReadingSession.getEndSessionPageNumber() >= book.getTotalPages();
                })
                .filter(isFinished -> isFinished)
                .count();
        Long booksReading = sessionsByBook.entrySet().stream()
                .map(entry -> {
                    Book book = entry.getKey();
                    ReadingSession latestReadingSession = entry.getValue().get(0);
                    return latestReadingSession.getEndSessionPageNumber() < book.getTotalPages();
                })
                .filter(isFinished -> isFinished)
                .count();
        // totalPagesRead
        Long totalPagesRead = (long) readingSessionList.stream()
                .mapToInt(ReadingSession::getPagesReadInSession)
                .sum();
        // pagesReadPerDay
        Map<LocalDate, Long> pagesReadPerDay = readingSessionList.stream()
                .collect(Collectors.groupingBy(
                        session -> session.getSessionDateTime().toLocalDate(),
                        Collectors.summingLong(ReadingSession::getPagesReadInSession)
                ));
        // Average Rating
        Double avgRating = reviewRepository.findAverageRatingByUserId(userId);
        // Streak calculation
        long streak = 0;
        LocalDate previousDate = LocalDate.now();
        for(ReadingSession readingSession: readingSessionList) {
            LocalDate currentDate = readingSession.getSessionDateTime().toLocalDate();
            long daysBetween = ChronoUnit.DAYS.between(currentDate, previousDate);
            if(daysBetween == 0L) {
                continue;
            }
            if(daysBetween > 1) {
                break;
            }
            streak++;
            previousDate = currentDate;
        }
        return new StatsResponseDTO(
                totalBooks,
                booksNotStarted,
                booksReading,
                booksFinished,
                totalPagesRead,
                pagesReadPerDay,
                avgRating,
                streak

        );
    }

    // Utility methods
    void validateUserExists(Integer userId) {
        if(!userRepository.existsById(userId)) {
            throw new UserNotFoundException(
                    environment.getProperty("Service.USER_NOT_FOUND")
            );
        };
    };
}
