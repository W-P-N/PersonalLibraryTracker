package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.readingSessionDTOs.ReadingSessionRequestDTO;
import com.wpn.personallibrarytracker.dto.readingSessionDTOs.ReadingSessionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReadingSessionService {
    ReadingSessionResponseDTO logSession(Integer userId, Integer bookId, ReadingSessionRequestDTO readingSessionRequestDTO);
    Page<ReadingSessionResponseDTO> getSessions(Integer userId, Integer bookId, Pageable pageable);
    ReadingSessionResponseDTO getSessionById(Integer userId, Integer bookId, Integer sessionId);
    ReadingSessionResponseDTO updateSession(Integer userId, Integer bookId, Integer sessionId, ReadingSessionRequestDTO readingSessionRequestDTO);
    void deleteSession(Integer userId, Integer bookId, Integer sessionId);
}
