package com.wpn.personallibrarytracker.repository;

import com.wpn.personallibrarytracker.entity.ReadingSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Integer> {
    Page<ReadingSession> findByBookBookIdAndBookUserUserId(
            Integer bookId, Integer userId, Pageable pageable);
    Optional<ReadingSession> findTopByBookBookIdAndBookUserUserIdOrderBySessionDateTimeDesc(
            Integer bookId, Integer userId);
    Optional<ReadingSession> findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeAfterOrderBySessionDateTimeAsc(
            Integer bookId,
            Integer userId,
            LocalDateTime dateTime
    );
    Optional<ReadingSession> findFirstByBookBookIdAndBookUserUserIdAndSessionDateTimeBeforeOrderBySessionDateTimeDesc(
            Integer bookId,
            Integer userId,
            LocalDateTime dateTime
    );
    Optional<ReadingSession> findByReadingSessionIdAndBookBookIdAndBookUserUserId(
            Integer readingSessionId, Integer bookId, Integer userId);

}
