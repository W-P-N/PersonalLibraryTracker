package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.readingSessionDTOs.ReadingSessionRequestDTO;
import com.wpn.personallibrarytracker.dto.readingSessionDTOs.ReadingSessionResponseDTO;
import com.wpn.personallibrarytracker.service.ReadingSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/books/{bookId}/sessions")
@Validated
public class ReadingSessionController {

    @Autowired
    ReadingSessionService readingSessionService;

    @PostMapping
    public ResponseEntity<ReadingSessionResponseDTO> addSession(
            @PathVariable Integer userId,
            @PathVariable Integer bookId,
            @RequestBody ReadingSessionRequestDTO readingSessionRequestDTO
            ) {

        return new ResponseEntity<>(
                readingSessionService.logSession(
                        userId, bookId, readingSessionRequestDTO
                ),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<Page<ReadingSessionResponseDTO>> getSessions(
            @PathVariable Integer userId,
            @PathVariable Integer bookId,
            @RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "5") Integer pageSize
    ) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return ResponseEntity.ok(
                readingSessionService.getSessions(userId, bookId, pageable)
        );
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ReadingSessionResponseDTO> getSessionById(
            @PathVariable Integer userId,
            @PathVariable Integer bookId,
            @PathVariable Integer sessionId
    ) {
        return ResponseEntity.ok(
                readingSessionService.getSessionById(userId, bookId, sessionId)
        );
    }

    @PatchMapping("/{sessionId}")
    public ResponseEntity<ReadingSessionResponseDTO> updateSession(
            @PathVariable Integer userId,
            @PathVariable Integer bookId,
            @PathVariable Integer sessionId,
            @RequestBody ReadingSessionRequestDTO readingSessionRequestDTO
    ) {
        return ResponseEntity.ok(
                readingSessionService.updateSession(
                        userId, bookId, sessionId, readingSessionRequestDTO
                )
        );
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @PathVariable Integer userId,
            @PathVariable Integer bookId,
            @PathVariable Integer sessionId
    ) {
        readingSessionService.deleteSession(userId, bookId, sessionId);
        return ResponseEntity.noContent().build();
    }
}
