package com.wpn.personallibrarytracker.projections;

import java.time.LocalDateTime;

public interface ReadingSessionStatsProjection {
    Integer getReadingSessionId();
    LocalDateTime getSessionDateTime();
    Integer getEndSessionPageNumber();
    Integer getPagesReadInSession();
    Integer getBookId();
    Integer getBookTotalPages();
}
