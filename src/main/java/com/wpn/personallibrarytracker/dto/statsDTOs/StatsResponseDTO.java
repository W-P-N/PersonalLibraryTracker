package com.wpn.personallibrarytracker.dto.statsDTOs;

import java.time.LocalDate;
import java.util.Map;

public record StatsResponseDTO(
        Long totalBooks,
        Long booksNotStarted,
        Long booksReading,
        Long booksFinished,
        Long totalPagesRead,
        Map<LocalDate, Long> pagesReadPerDay,
        Double averageRating,
        Long currentStreak
) {};
