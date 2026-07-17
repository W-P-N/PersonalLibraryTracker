package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.noteDTOs.NoteDetailsResponseDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteRequestDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteResponseDTO;
import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.Note;
import com.wpn.personallibrarytracker.exceptions.BookNotFoundForUserException;
import com.wpn.personallibrarytracker.exceptions.InvalidPageNumberException;
import com.wpn.personallibrarytracker.exceptions.NoteNotFoundException;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.repository.BookRepository;
import com.wpn.personallibrarytracker.repository.NoteRepository;
import com.wpn.personallibrarytracker.repository.UserRepository;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service("noteService")
public class NoteServiceImpl implements NoteService{
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final NoteRepository noteRepository;
    private final Environment environment;

    public NoteServiceImpl(
            UserRepository userRepository,
            BookRepository bookRepository,
            NoteRepository noteRepository,
            Environment environment
    ) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.noteRepository = noteRepository;
        this.environment = environment;
    }

    @Override
    @Transactional
    public NoteDetailsResponseDTO createNote(
            Integer bookId,
            Integer userId,
            NoteRequestDTO noteRequestDTO
    ) {
        validateUserExists(userId);
        Book foundBook = getBookByUser(bookId, userId);
        if(
                noteRequestDTO.pageNumber() != null &&
                noteRequestDTO.pageNumber() > foundBook.getTotalPages()
        ) {
            throw new InvalidPageNumberException(
                    environment.getProperty("Service.PAGE_NUMBER_EXCEEDS_BOOK")
            );
        };
        Note newNote = new Note();
        newNote.setContent(noteRequestDTO.content());
        newNote.setPageNumber(noteRequestDTO.pageNumber());
        newNote.setBook(foundBook);
        newNote.setCreatedAt(LocalDateTime.now());
        Note savedNote = noteRepository.save(newNote);
        return new NoteDetailsResponseDTO(
                savedNote.getNoteId(),
                savedNote.getContent(),
                savedNote.getCreatedAt(),
                savedNote.getPageNumber()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteResponseDTO> getNotes(
            Integer bookId, Integer userId, Pageable pageable
    ) {
        validateUserExists(userId);
        validateBookByUserExists(bookId, userId);
        Page<Note> notePages = noteRepository.findByBookBookIdAndBookUserUserId(
                bookId,
                userId,
                pageable
        );
        return notePages.map(
                notePage -> new NoteResponseDTO(
                        notePage.getNoteId(),
                        notePage.getCreatedAt(),
                        notePage.getPageNumber()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public NoteDetailsResponseDTO getNoteById(
            Integer noteId, Integer bookId, Integer userId
    ) {
        validateUserExists(userId);
        validateBookByUserExists(bookId, userId);
        Note foundNote = getNoteByBookAndUser(noteId, bookId, userId);
        return new NoteDetailsResponseDTO(
                foundNote.getNoteId(),
                foundNote.getContent(),
                foundNote.getCreatedAt(),
                foundNote.getPageNumber()
        );
    }

    @Override
    @Transactional
    public NoteDetailsResponseDTO updateNote(
            Integer noteId,
            Integer bookId,
            Integer userId,
            NoteRequestDTO noteRequestDTO
    ) {
        validateUserExists(userId);
        Book foundBook = getBookByUser(bookId, userId);
        if(
                noteRequestDTO.pageNumber() != null && 
                noteRequestDTO.pageNumber() > foundBook.getTotalPages()
        ) {
            throw new InvalidPageNumberException(
                    environment.getProperty("Service.PAGE_NUMBER_EXCEEDS_BOOK")
            );
        };
        Note foundNote = getNoteByBookAndUser(noteId, bookId, userId);
        if(noteRequestDTO.pageNumber() != null) {
            foundNote.setPageNumber(noteRequestDTO.pageNumber());
        }
        if(noteRequestDTO.content() != null) {
            foundNote.setContent(noteRequestDTO.content());
        }
        Note updatedNote = noteRepository.save(foundNote);
        return new NoteDetailsResponseDTO(
                updatedNote.getNoteId(),
                updatedNote.getContent(),
                updatedNote.getCreatedAt(),
                updatedNote.getPageNumber()
        );
    }

    @Override
    @Transactional
    public void deleteNote(
            Integer noteId, Integer bookId, Integer userId
    ) {
        validateUserExists(userId);
        validateBookByUserExists(bookId, userId);
        Note foundNote = getNoteByBookAndUser(
                noteId, bookId, userId
        );
        noteRepository.delete(foundNote);
    }

    // Utility functions
    void validateUserExists(Integer userId) {
        if(!userRepository.existsById(userId)) {
            throw new UserNotFoundException(
                    environment.getProperty("Service.USER_NOT_FOUND")
            );
        };
    };

    void validateBookByUserExists(Integer bookId, Integer userId) {
        if(!bookRepository.existsByBookIdAndUserUserId(bookId, userId)) {
            throw new BookNotFoundForUserException(
                    environment.getProperty("Service.BOOK_NOT_FOUND_FOR_USER")
            );
        };
    };

    Book getBookByUser(Integer bookId, Integer userId) {
        return bookRepository.findByBookIdAndUserUserId(bookId, userId)
                .orElseThrow(() -> new BookNotFoundForUserException(
                        environment.getProperty("Service.BOOK_NOT_FOUND_FOR_USER")
                ));
    };

    Note getNoteByBookAndUser(Integer noteId, Integer bookId, Integer userId) {
        return noteRepository.findByNoteIdAndBookBookIdAndBookUserUserId(
                noteId,
                bookId,
                userId
        )
        .orElseThrow(() -> new NoteNotFoundException(
                environment.getProperty("Service.NOTE_NOT_FOUND")
        ));
    }

}
