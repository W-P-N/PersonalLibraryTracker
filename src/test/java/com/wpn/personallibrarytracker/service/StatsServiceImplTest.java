package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.repository.BookRepository;
import com.wpn.personallibrarytracker.repository.ReadingSessionRepository;
import com.wpn.personallibrarytracker.repository.ReviewRepository;
import com.wpn.personallibrarytracker.repository.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import com.wpn.personallibrarytracker.dto.statsDTOs.StatsResponseDTO;
import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.ReadingSession;
import com.wpn.personallibrarytracker.projections.ReadingSessionStatsProjection;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StatsServiceImplTest {
    private ReadingSessionStatsProjection mockStatsProjection(Integer id, Integer pagesRead, Integer endPage, java.time.LocalDateTime dateTime, Integer bookId, Integer bookTotalPages) {
        ReadingSessionStatsProjection projection = org.mockito.Mockito.mock(ReadingSessionStatsProjection.class);
        org.mockito.Mockito.lenient().when(projection.getReadingSessionId()).thenReturn(id);
        org.mockito.Mockito.lenient().when(projection.getPagesReadInSession()).thenReturn(pagesRead);
        org.mockito.Mockito.lenient().when(projection.getEndSessionPageNumber()).thenReturn(endPage);
        org.mockito.Mockito.lenient().when(projection.getSessionDateTime()).thenReturn(dateTime);
        org.mockito.Mockito.lenient().when(projection.getBookId()).thenReturn(bookId);
        org.mockito.Mockito.lenient().when(projection.getBookTotalPages()).thenReturn(bookTotalPages);
        return projection;
    }

    @Mock
    UserRepository userRepository;
    @Mock
    BookRepository bookRepository;
    @Mock
    ReviewRepository reviewRepository;
    @Mock
    ReadingSessionRepository readingSessionRepository;
    @Mock
    Environment environment;
    @InjectMocks
    StatsServiceImpl statsService;

    @Test
    void getStats_happyPath_shouldReturnStats() {
        // Arrange
        Integer userId = 1;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookRepository.countByUserUserId(userId)).thenReturn(3L);

        Book book1 = new Book();
        book1.setBookId(1);
        book1.setTotalPages(100);

        Book book2 = new Book();
        book2.setBookId(2);
        book2.setTotalPages(200);

        LocalDate today = LocalDate.now();

        ReadingSessionStatsProjection session1 = mockStatsProjection(1, 30, 50, LocalDateTime.of(today.minusDays(1), LocalTime.NOON), book1.getBookId(), book1.getTotalPages());
        ReadingSessionStatsProjection session2 = mockStatsProjection(2, 20, 20, LocalDateTime.of(today.minusDays(2), LocalTime.NOON), book1.getBookId(), book1.getTotalPages());
        ReadingSessionStatsProjection session3 = mockStatsProjection(3, 50, 200, LocalDateTime.of(today.minusDays(3), LocalTime.NOON), book2.getBookId(), book2.getTotalPages());

        List<ReadingSessionStatsProjection> sessions = new ArrayList<>();
        sessions.add(session1);
        sessions.add(session2);
        sessions.add(session3);

        when(readingSessionRepository.findAllSessionsWithComputedPagesByUser(userId)).thenReturn(sessions);
        when(reviewRepository.findAverageRatingByUserId(userId)).thenReturn(4.5);

        // Act
        StatsResponseDTO stats = statsService.getStats(userId);

        // Assert
        assertNotNull(stats);
        assertEquals(3L, stats.totalBooks());
        assertEquals(1L, stats.booksNotStarted());
        assertEquals(1L, stats.booksReading());
        assertEquals(1L, stats.booksFinished());
        assertEquals(100L, stats.totalPagesRead());
        assertEquals(4.5, stats.averageRating());
        
        assertEquals(3L, stats.currentStreak());

        assertEquals(3, stats.pagesReadPerDay().size());
        assertEquals(30L, stats.pagesReadPerDay().get(today.minusDays(1)));
        assertEquals(20L, stats.pagesReadPerDay().get(today.minusDays(2)));
        assertEquals(50L, stats.pagesReadPerDay().get(today.minusDays(3)));
    }

    @Test
    void getStats_unhappyPath_whenUserHasNoBooks_shouldReturnEmptyStats() {
        // Arrange
        Integer userId = 2;
        when(userRepository.existsById(userId)).thenReturn(true);
        when(bookRepository.countByUserUserId(userId)).thenReturn(0L);
        when(readingSessionRepository.findAllSessionsWithComputedPagesByUser(userId)).thenReturn(Collections.emptyList());
        when(reviewRepository.findAverageRatingByUserId(userId)).thenReturn(null);

        // Act
        StatsResponseDTO stats = statsService.getStats(userId);

        // Assert
        assertNotNull(stats);
        assertEquals(0L, stats.totalBooks());
        assertEquals(0L, stats.booksNotStarted());
        assertEquals(0L, stats.booksReading());
        assertEquals(0L, stats.booksFinished());
        assertEquals(0L, stats.totalPagesRead());
        assertNull(stats.averageRating());
        assertEquals(0L, stats.currentStreak());
        assertTrue(stats.pagesReadPerDay().isEmpty());
    }

    @Test
    void getStats_unhappyPath_whenUserNotFound_shouldThrowException() {
        // Arrange
        Integer userId = 999;
        when(userRepository.existsById(userId)).thenReturn(false);
        when(environment.getProperty("Service.USER_NOT_FOUND")).thenReturn("User not found");

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> statsService.getStats(userId));
        assertEquals("User not found", exception.getMessage());
    }
}
