package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.noteDTOs.NoteDetailsResponseDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteRequestDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteResponseDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteUpdateRequestDTO;
import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.Note;
import com.wpn.personallibrarytracker.exceptions.InvalidPageNumberException;
import com.wpn.personallibrarytracker.exceptions.ResourceNotFoundException;
import com.wpn.personallibrarytracker.repository.BookRepository;
import com.wpn.personallibrarytracker.repository.NoteRepository;
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
    BookRepository bookRepository;
    @Mock
    NoteRepository noteRepository;
    @Mock
    Environment environment;

    @InjectMocks
    NoteServiceImpl noteService;

    @Test
    void createNote_happyPath_shouldReturnNoteDetailsResponseDTO() {
        NoteRequestDTO request = new NoteRequestDTO("Test Content", 10);
        Book book = new Book();
        book.setBookId(1);
        book.setTotalPages(100);

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
    void createNote_unHappyPath_shouldThrowResourceNotFoundException_whenBookNotFoundForUser() {
        when(bookRepository.findByBookIdAndUserUserId(1, 1)).thenReturn(Optional.empty());
        when(environment.getProperty("Service.RESOURCE_NOT_FOUND"))
                .thenReturn("The requested resource was not found");

        NoteRequestDTO request = new NoteRequestDTO("Test Content", 10);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> noteService.createNote(1, 1, request));
        verify(noteRepository, never()).save(any());
    }

    @Test
    void createNote_unHappyPath_shouldThrowInvalidPageNumberException_whenPageExceedsBook() {
        NoteRequestDTO request = new NoteRequestDTO("Test Content", 150);
        Book book = new Book();
        book.setBookId(1);
        book.setTotalPages(100);

        when(bookRepository.findByBookIdAndUserUserId(1, 1)).thenReturn(Optional.of(book));
        when(environment.getProperty("Service.PAGE_NUMBER_EXCEEDS_BOOK"))
                .thenReturn("Page number exceeds book");

        Assertions.assertThrows(InvalidPageNumberException.class, () -> noteService.createNote(1, 1, request));
        verify(noteRepository, never()).save(any());
    }

    @Test
    void getNotes_happyPath_shouldReturnPageNoteResponseDTO() {
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
    void getNotes_unHappyPath_shouldThrowResourceNotFoundException_whenBookNotFoundForUser() {
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(false);
        when(environment.getProperty("Service.RESOURCE_NOT_FOUND"))
                .thenReturn("The requested resource was not found");

        Pageable pageable = PageRequest.of(0, 10);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> noteService.getNotes(1, 1, pageable));
        verify(noteRepository, never()).findByBookBookIdAndBookUserUserId(any(), any(), any());
    }

    @Test
    void getNoteById_happyPath_shouldReturnNoteDetailsResponseDTO() {
        Note note = new Note();
        note.setNoteId(100);
        note.setContent("Test Content");
        note.setPageNumber(10);
        note.setCreatedAt(LocalDateTime.now());

        when(noteRepository.findByNoteIdAndBookBookIdAndBookUserUserId(100, 1, 1)).thenReturn(Optional.of(note));

        NoteDetailsResponseDTO result = noteService.getNoteById(100, 1, 1);

        Assertions.assertEquals(100, result.noteId());
        Assertions.assertEquals("Test Content", result.content());
        verify(bookRepository, never()).existsByBookIdAndUserUserId(any(), any());
    }

    @Test
    void getNoteById_unHappyPath_shouldThrowResourceNotFoundException_whenNoteNotFound() {
        when(noteRepository.findByNoteIdAndBookBookIdAndBookUserUserId(100, 1, 1)).thenReturn(Optional.empty());
        when(environment.getProperty("Service.RESOURCE_NOT_FOUND"))
                .thenReturn("The requested resource was not found");

        Assertions.assertThrows(ResourceNotFoundException.class, () -> noteService.getNoteById(100, 1, 1));
    }

    @Test
    void updateNote_happyPath_shouldReturnNoteDetailsResponseDTO() {
        Book foundBook = new Book();
        foundBook.setBookId(1);
        foundBook.setTotalPages(100);
        when(bookRepository.findByBookIdAndUserUserId(1, 1))
                .thenReturn(Optional.of(foundBook));

        Note note = new Note();
        note.setNoteId(100);
        note.setContent("Old Content");
        note.setPageNumber(5);
        note.setCreatedAt(LocalDateTime.now());

        when(noteRepository.findByNoteIdAndBookBookIdAndBookUserUserId(100, 1, 1)).thenReturn(Optional.of(note));
        when(noteRepository.save(any(Note.class))).thenReturn(note);

        NoteUpdateRequestDTO request = new NoteUpdateRequestDTO("New Content", 10);
        NoteDetailsResponseDTO result = noteService.updateNote(
                note.getNoteId(), foundBook.getBookId(), 1, request
        );

        Assertions.assertEquals(100, result.noteId());
        Assertions.assertEquals("New Content", result.content());
        Assertions.assertEquals(10, result.pageNumber());
    }

    @Test
    void updateNote_unHappyPath_shouldThrowResourceNotFoundException_whenBookNotFoundForUser() {
        when(bookRepository.findByBookIdAndUserUserId(1, 1)).thenReturn(Optional.empty());
        when(environment.getProperty("Service.RESOURCE_NOT_FOUND"))
                .thenReturn("The requested resource was not found");

        NoteUpdateRequestDTO request = new NoteUpdateRequestDTO("New Content", 10);
        Assertions.assertThrows(ResourceNotFoundException.class, () -> noteService.updateNote(100, 1, 1, request));
        verify(noteRepository, never()).save(any());
    }

    @Test
    void updateNote_unHappyPath_shouldThrowResourceNotFoundException_whenNoteNotFound() {
        Book book = new Book();
        book.setBookId(1);
        book.setTotalPages(200);

        when(bookRepository.findByBookIdAndUserUserId(1, 1)).thenReturn(Optional.of(book));
        when(noteRepository.findByNoteIdAndBookBookIdAndBookUserUserId(100, 1, 1))
                .thenReturn(Optional.empty());
        when(environment.getProperty("Service.RESOURCE_NOT_FOUND"))
                .thenReturn("The requested resource was not found");

        NoteUpdateRequestDTO request = new NoteUpdateRequestDTO("New Content", 10);

        Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> noteService.updateNote(100, 1, 1, request)
        );

        verify(noteRepository, never()).save(any());
    }

    @Test
    void deleteNote_happyPath_shouldDeleteNoteWithGivenId() {
        Note note = new Note();
        note.setNoteId(100);

        when(noteRepository.findByNoteIdAndBookBookIdAndBookUserUserId(100, 1, 1)).thenReturn(Optional.of(note));

        noteService.deleteNote(100, 1, 1);

        verify(noteRepository, times(1)).delete(note);
        verify(bookRepository, never()).existsByBookIdAndUserUserId(any(), any());
    }

    @Test
    void deleteNote_unHappyPath_shouldThrowResourceNotFoundException_whenNoteNotFound() {
        when(noteRepository.findByNoteIdAndBookBookIdAndBookUserUserId(100, 1, 1)).thenReturn(Optional.empty());
        when(environment.getProperty("Service.RESOURCE_NOT_FOUND"))
                .thenReturn("The requested resource was not found");

        Assertions.assertThrows(ResourceNotFoundException.class, () -> noteService.deleteNote(100, 1, 1));
        verify(noteRepository, never()).delete(any());
    }
}
