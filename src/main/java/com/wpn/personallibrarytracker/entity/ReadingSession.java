package com.wpn.personallibrarytracker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class ReadingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer readingSessionId;
    private LocalDateTime sessionDateTime;
    private Integer endSessionPageNumber;
    private Integer pagesReadInSession;
}
    