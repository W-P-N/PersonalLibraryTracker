package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.noteDTOs.NoteDetailsResponseDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteRequestDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteResponseDTO;
import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.Note;
import com.wpn.personallibrarytracker.exceptions.BookNotFoundForUserException;
import com.wpn.personallibrarytracker.exceptions.NoteNotFoundException;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.repository.BookRepository;
import com.wpn.personallibrarytracker.repository.NoteRepository;
import com.wpn.personallibrarytracker.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NoteServiceImplTest {
    @Mock
    UserRepository userRepository;
    @Mock
    BookRepository bookRepository;
    @Mock
    NoteRepository noteRepository;
    @Mock
    Environment environment;
    
    @InjectMocks
    NoteServiceImpl noteService;

    // --- Create Note Tests ---
    @Test
    void createNote_happyPath_shouldReturnNoteDetailsResponseDTO() {
        NoteRequestDTO request = new NoteRequestDTO("Test Content", 10);
        Book book = new Book();
        book.setBookId(1);
        
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.findByBookIdAndUserUserId(1, 1)).thenReturn(Optional.of(book));
        
        Note savedNote = new Note();
        savedNote.setNoteId(100);
        savedNote.setContent("Test Content");
        savedNote.setPageNumber(10);
        savedNote.setBook(book);
        savedNote.setCreatedAt(LocalDateTime.now());
        
        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        NoteDetailsResponseDTO response = noteService.createNote(1, 1, request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(100, response.noteId());
        Assertions.assertEquals("Test Content", response.content());
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void createNote_unHappyPath_shouldThrowUserNotExistsException() {
        when(userRepository.existsById(1)).thenReturn(false);
        when(environment.getProperty("Service.USER_NOT_FOUND")).thenReturn("User not found");

        NoteRequestDTO request = new NoteRequestDTO("Test Content", 10);
        Assertions.assertThrows(UserNotFoundException.class, () -> noteService.createNote(1, 1, request));
    }

    @Test
    void createNote_unHappyPath_shouldThrowBookNotExistsForUserException() {
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.findByBookIdAndUserUserId(1, 1)).thenReturn(Optional.empty());
        when(environment.getProperty("Service.BOOK_NOT_FOUND_FOR_USER")).thenReturn("Book not found for user");

        NoteRequestDTO request = new NoteRequestDTO("Test Content", 10);
        Assertions.assertThrows(BookNotFoundForUserException.class, () -> noteService.createNote(1, 1, request));
    }

    // --- Get Notes Tests ---
    @Test
    void getNotes_happyPath_shouldReturnPageNoteResponseDTO() {
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(true);

        Note note = new Note();
        note.setNoteId(100);
        note.setContent("Test");
        note.setPageNumber(10);
        note.setCreatedAt(LocalDateTime.now());
        
        Page<Note> page = new PageImpl<>(Collections.singletonList(note));
        Pageable pageable = PageRequest.of(0, 10);
        when(noteRepository.findByBookBookIdAndBookUserUserId(1, 1, pageable)).thenReturn(page);

        Page<NoteResponseDTO> result = noteService.getNotes(1, 1, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(100, result.getContent().get(0).noteId());
    }

    @Test
    void getNotes_unHappyPath_shouldThrowUserNotFoundException() {
        when(userRepository.existsById(1)).thenReturn(false);
        when(environment.getProperty("Service.USER_NOT_FOUND")).thenReturn("User not found");

        Pageable pageable = PageRequest.of(0, 10);
        Assertions.assertThrows(UserNotFoundException.class, () -> noteService.getNotes(1, 1, pageable));
    }

    @Test
    void getNotes_unHappyPath_shouldThrowBookNotFoundForUserException() {
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(false);
        when(environment.getProperty("Service.BOOK_NOT_FOUND_FOR_USER")).thenReturn("Book not found");

        Pageable pageable = PageRequest.of(0, 10);
        Assertions.assertThrows(BookNotFoundForUserException.class, () -> noteService.getNotes(1, 1, pageable));
    }

    // --- Get Note By Id Tests ---
    @Test
    void getNoteById_happyPath_shouldReturnNoteDetailsResponseDTO() {
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(true);
        
        Note note = new Note();
        note.setNoteId(100);
        note.setContent("Test Content");
        note.setPageNumber(10);
        note.setCreatedAt(LocalDateTime.now());

        when(noteRepository.findByNoteIdAndBookBookIdAndBookUserUserId(100, 1, 1)).thenReturn(Optional.of(note));

        NoteDetailsResponseDTO result = noteService.getNoteById(100, 1, 1);

        Assertions.assertEquals(100, result.noteId());
        Assertions.assertEquals("Test Content", result.content());
    }

    @Test
    void getNoteById_unHappyPath_shouldThrowUserNotFoundException() {
        when(userRepository.existsById(1)).thenReturn(false);
        when(environment.getProperty("Service.USER_NOT_FOUND")).thenReturn("User not found");

        Assertions.assertThrows(UserNotFoundException.class, () -> noteService.getNoteById(100, 1, 1));
    }

    @Test
    void getNoteById_unHappyPath_shouldThrowBookNotFoundForUserException() {
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(false);
        when(environment.getProperty("Service.BOOK_NOT_FOUND_FOR_USER")).thenReturn("Book not found");

        Assertions.assertThrows(BookNotFoundForUserException.class, () -> noteService.getNoteById(100, 1, 1));
    }

    @Test
    void getNoteById_unHappyPath_shouldThrowNoteNotFoundException() {
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(true);
        when(noteRepository.findByNoteIdAndBookBookIdAndBookUserUserId(100, 1, 1)).thenReturn(Optional.empty());
        when(environment.getProperty("Service.NOTE_NOT_FOUND")).thenReturn("Note not found");

        Assertions.assertThrows(NoteNotFoundException.class, () -> noteService.getNoteById(100, 1, 1));
    }

    // --- Update Note Tests ---
    @Test
    void updateNote_happyPath_shouldReturnNoteDetailsResponseDTO() {
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(true);
        
        Note note = new Note();
        note.setNoteId(100);
        note.setContent("Old Content");
        note.setPageNumber(5);
        note.setCreatedAt(LocalDateTime.now());

        when(noteRepository.findByNoteIdAndBookBookIdAndBookUserUserId(100, 1, 1)).thenReturn(Optional.of(note));
        when(noteRepository.save(any(Note.class))).thenReturn(note); 

        NoteRequestDTO request = new NoteRequestDTO("New Content", 10);
        NoteDetailsResponseDTO result = noteService.updateNote(100, 1, 1, request);

        Assertions.assertEquals(100, result.noteId());
        Assertions.assertEquals("New Content", result.content());
        Assertions.assertEquals(10, result.pageNumber());
    }

    @Test
    void updateNote_unHappyPath_shouldThrowUserNotFoundException() {
        when(userRepository.existsById(1)).thenReturn(false);
        when(environment.getProperty("Service.USER_NOT_FOUND")).thenReturn("User not found");

        NoteRequestDTO request = new NoteRequestDTO("New Content", 10);
        Assertions.assertThrows(UserNotFoundException.class, () -> noteService.updateNote(100, 1, 1, request));
    }

    @Test
    void updateNote_unHappyPath_shouldThrowBookNotFoundForUserException() {
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(false);
        when(environment.getProperty("Service.BOOK_NOT_FOUND_FOR_USER")).thenReturn("Book not found");

        NoteRequestDTO request = new NoteRequestDTO("New Content", 10);
        Assertions.assertThrows(BookNotFoundForUserException.class, () -> noteService.updateNote(100, 1, 1, request));
    }

    @Test
    void updateNote_unHappyPath_shouldThrowNoteNotFoundException() {
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(true);
        when(noteRepository.findByNoteIdAndBookBookIdAndBookUserUserId(100, 1, 1)).thenReturn(Optional.empty());
        when(environment.getProperty("Service.NOTE_NOT_FOUND")).thenReturn("Note not found");

        NoteRequestDTO request = new NoteRequestDTO("New Content", 10);
        Assertions.assertThrows(NoteNotFoundException.class, () -> noteService.updateNote(100, 1, 1, request));
    }

    // --- Delete Note Tests ---
    @Test
    void deleteNote_happyPath_shouldDeleteNoteWithGivenId() {
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(true);
        
        Note note = new Note();
        note.setNoteId(100);

        when(noteRepository.findByNoteIdAndBookBookIdAndBookUserUserId(100, 1, 1)).thenReturn(Optional.of(note));
        
        noteService.deleteNote(100, 1, 1);

        verify(noteRepository, times(1)).delete(note);
    }

    @Test
    void deleteNote_unHappyPath_shouldThrowUserNotFoundException() {
        when(userRepository.existsById(1)).thenReturn(false);
        when(environment.getProperty("Service.USER_NOT_FOUND")).thenReturn("User not found");

        Assertions.assertThrows(UserNotFoundException.class, () -> noteService.deleteNote(100, 1, 1));
    }

    @Test
    void deleteNote_unHappyPath_shouldThrowBookNotFoundForUserException() {
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(false);
        when(environment.getProperty("Service.BOOK_NOT_FOUND_FOR_USER")).thenReturn("Book not found");

        Assertions.assertThrows(BookNotFoundForUserException.class, () -> noteService.deleteNote(100, 1, 1));
    }

    @Test
    void deleteNote_unHappyPath_shouldThrowNoteNotFoundException() {
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(true);
        when(noteRepository.findByNoteIdAndBookBookIdAndBookUserUserId(100, 1, 1)).thenReturn(Optional.empty());
        when(environment.getProperty("Service.NOTE_NOT_FOUND")).thenReturn("Note not found");

        Assertions.assertThrows(NoteNotFoundException.class, () -> noteService.deleteNote(100, 1, 1));
    }
}
