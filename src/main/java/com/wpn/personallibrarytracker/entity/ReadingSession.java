package com.wpn.personallibrarytracker.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name="reading_sessions")
public class ReadingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer readingSessionId;
    private LocalDateTime sessionDateTime;
    private Integer endSessionPageNumber;
    private Integer pagesReadInSession;
}
    