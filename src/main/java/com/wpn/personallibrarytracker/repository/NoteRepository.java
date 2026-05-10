package com.wpn.personallibrarytracker.repository;

import com.wpn.personallibrarytracker.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Integer> {
}
