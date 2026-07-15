package com.wpn.personallibrarytracker.repository;

import com.wpn.personallibrarytracker.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Integer> {
    Page<Note> findByBookBookIdAndBookUserUserId(
            Integer bookId, Integer userId, Pageable pageable
    );
    Optional<Note> findByNoteIdAndBookBookIdAndBookUserUserId(
            Integer noteId, Integer bookId, Integer userId
    );
}
