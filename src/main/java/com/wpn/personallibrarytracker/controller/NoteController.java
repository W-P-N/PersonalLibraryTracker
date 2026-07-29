package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.noteDTOs.NoteDetailsResponseDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteRequestDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteResponseDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteUpdateRequestDTO;
import com.wpn.personallibrarytracker.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books/{bookId}/notes")
@Validated
public class NoteController {
    private final NoteService noteService;

    public NoteController(
            NoteService noteService
    ) {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<NoteDetailsResponseDTO> addNote(
            @AuthenticationPrincipal Integer userId,
            @PathVariable Integer bookId,
            @RequestBody @Valid NoteRequestDTO noteRequestDTO
    ) {
        return new ResponseEntity<>(
                noteService.createNote(
                        bookId,
                        userId,
                        noteRequestDTO
                ),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<Page<NoteResponseDTO>> getNotes(
            @AuthenticationPrincipal Integer userId,
            @PathVariable Integer bookId,
            @RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "5") Integer pageSize
    ) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return ResponseEntity.ok(
                noteService.getNotes(
                        bookId,
                        userId,
                        pageable
                )
        );
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<NoteDetailsResponseDTO> getNoteById(
            @AuthenticationPrincipal Integer userId,
            @PathVariable Integer bookId,
            @PathVariable Integer noteId
    ) {
        return ResponseEntity.ok(
                noteService.getNoteById(
                        noteId,
                        bookId,
                        userId
                )
        );
    }

    @PatchMapping("/{noteId}")
    public ResponseEntity<NoteDetailsResponseDTO> updateNote(
            @AuthenticationPrincipal Integer userId,
            @PathVariable Integer bookId,
            @PathVariable Integer noteId,
            @RequestBody @Valid NoteUpdateRequestDTO noteUpdateRequestDTO
    ) {
        return ResponseEntity.ok(
                noteService.updateNote(
                        noteId,
                        bookId,
                        userId,
                        noteUpdateRequestDTO
                )
        );
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteNote(
            @AuthenticationPrincipal Integer userId,
            @PathVariable Integer bookId,
            @PathVariable Integer noteId
    ) {
        noteService.deleteNote(noteId, bookId, userId);
        return ResponseEntity.noContent().build();
    }
}
