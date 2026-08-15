package com.wpn.personallibrarytracker.projections;

import com.wpn.personallibrarytracker.entity.Book;

import java.time.LocalDateTime;

public interface ReadingSessionProjection {
    Integer getReadingSessionId();
    LocalDateTime getSessionDateTime();
    Integer getEndSessionPageNumber();
    Integer getPagesReadInSession();
}
