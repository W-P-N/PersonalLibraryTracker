package com.wpn.personallibrarytracker.repository;

import com.wpn.personallibrarytracker.entity.ReadingSession;
import com.wpn.personallibrarytracker.projections.ReadingSessionProjection;
import com.wpn.personallibrarytracker.projections.ReadingSessionStatsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
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
    List<ReadingSession> findAllByBookUserUserIdOrderBySessionDateTimeDesc(Integer userId);

    @Query(
            value = """
                SELECT
                    rs.reading_session_id AS readingSessionId,
                    rs.session_date_time AS sessionDateTime,
                    rs.end_session_page_number AS endSessionPageNumber,
                    rs.end_session_page_number - LAG(rs.end_session_page_number, 1, 0)
                        OVER (PARTITION BY rs.book_id ORDER BY rs.session_date_time ASC) AS pagesReadInSession
                FROM reading_sessions rs
                JOIN books b ON rs.book_id = b.book_id
                WHERE rs.book_id = :bookId AND b.user_id = :userId
                ORDER BY rs.session_date_time DESC
            """,
            countQuery = """
                    SELECT COUNT(*)
                        FROM reading_sessions rs
                        JOIN books b ON rs.book_id = b.book_id
                        WHERE rs.book_id = :bookId AND b.user_id = :userId
            """,
            nativeQuery = true
    )
    Page<ReadingSessionProjection> findSessionsWithComputedPages(
            @Param("bookId") Integer bookId,
            @Param("userId") Integer userId,
            Pageable pageable
    );

    @Query(
            value = """
                SELECT * FROM (
                    SELECT
                        rs.reading_session_id AS readingSessionId,
                        rs.session_date_time AS sessionDateTime,
                        rs.end_session_page_number AS endSessionPageNumber,
                        rs.end_session_page_number - LAG(rs.end_session_page_number, 1, 0)
                            OVER (PARTITION BY rs.book_id ORDER BY rs.session_date_time ASC) AS pagesReadInSession
                    FROM reading_sessions rs
                    JOIN books b ON rs.book_id = b.book_id
                    WHERE rs.book_id = :bookId AND b.user_id = :userId
                ) AS computed_sessions
                WHERE readingSessionId = :sessionId
            """,
            nativeQuery = true
    )
    Optional<ReadingSessionProjection> findSessionWithComputedPages(
            @Param("sessionId") Integer sessionId,
            @Param("bookId") Integer bookId,
            @Param("userId") Integer userId
    );

    @Query(
            value = """
                SELECT
                    rs.reading_session_id AS readingSessionId,
                    rs.session_date_time AS sessionDateTime,
                    rs.end_session_page_number AS endSessionPageNumber,
                    rs.end_session_page_number - LAG(rs.end_session_page_number, 1, 0)
                        OVER (PARTITION BY rs.book_id ORDER BY rs.session_date_time ASC) AS pagesReadInSession,
                    rs.book_id AS bookId,
                    b.total_pages AS bookTotalPages
                FROM reading_sessions rs
                JOIN books b ON rs.book_id = b.book_id
                WHERE b.user_id = :userId
                ORDER BY rs.session_date_time DESC
            """,
            nativeQuery = true
    )
    List<ReadingSessionStatsProjection> findAllSessionsWithComputedPagesByUser(
            @Param("userId") Integer userId
    );
}
