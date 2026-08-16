package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.readingSessionDTOs.ReadingSessionRequestDTO;
import com.wpn.personallibrarytracker.dto.readingSessionDTOs.ReadingSessionDetailsResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReadingSessionService {
    ReadingSessionDetailsResponseDTO logSession(Integer userId, Integer bookId, ReadingSessionRequestDTO readingSessionRequestDTO);
    Page<ReadingSessionDetailsResponseDTO> getSessions(Integer userId, Integer bookId, Pageable pageable);
    ReadingSessionDetailsResponseDTO getSessionById(Integer userId, Integer bookId, Integer sessionId);
    ReadingSessionDetailsResponseDTO updateSession(Integer userId, Integer bookId, Integer sessionId, ReadingSessionRequestDTO readingSessionRequestDTO);
    void deleteSession(Integer userId, Integer bookId, Integer sessionId);
}
