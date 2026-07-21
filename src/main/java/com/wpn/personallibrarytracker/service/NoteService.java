package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.noteDTOs.NoteDetailsResponseDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteRequestDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteResponseDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteUpdateRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NoteService {
    NoteDetailsResponseDTO createNote(
            Integer bookId, Integer userId, NoteRequestDTO noteRequestDTO
    );
    Page<NoteResponseDTO> getNotes(
            Integer bookId, Integer userId, Pageable pageable
    );
    NoteDetailsResponseDTO getNoteById(
            Integer noteId, Integer bookId, Integer userId
    );
    NoteDetailsResponseDTO updateNote(
            Integer noteId,
            Integer bookId,
            Integer userId,
            NoteUpdateRequestDTO noteUpdateRequestDTO
    );
    void deleteNote(
            Integer noteId,
            Integer bookId,
            Integer userId
    );
}
