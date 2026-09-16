package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.noteDTOs.NoteDetailsResponseDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteRequestDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteResponseDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteUpdateRequestDTO;
import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.Note;
import com.wpn.personallibrarytracker.exceptions.ResourceNotFoundException;
import com.wpn.personallibrarytracker.exceptions.InvalidPageNumberException;
import com.wpn.personallibrarytracker.repository.BookRepository;
import com.wpn.personallibrarytracker.repository.NoteRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service("noteService")
public class NoteServiceImpl implements NoteService{
    private final MessageSource messageSource;
    private final BookRepository bookRepository;
    private final NoteRepository noteRepository;

    public NoteServiceImpl(
            MessageSource messageSource,
            BookRepository bookRepository,
            NoteRepository noteRepository
    ) {
        this.messageSource = messageSource;
        this.bookRepository = bookRepository;
        this.noteRepository = noteRepository;
    }

    @Override
    @Transactional
    public NoteDetailsResponseDTO createNote(
            Integer bookId,
            Integer userId,
            NoteRequestDTO noteRequestDTO
    ) {
        Book foundBook = getBookByUser(bookId, userId);
        if(
                noteRequestDTO.pageNumber() != null &&
                noteRequestDTO.pageNumber() > foundBook.getTotalPages()
        ) {
            throw new InvalidPageNumberException(
                    messageSource.getMessage("Service.PAGE_NUMBER_EXCEEDS_BOOK", null, LocaleContextHolder.getLocale())
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
        if(!bookRepository.existsByBookIdAndUserUserId(bookId, userId)) {
            throw new ResourceNotFoundException(
                    messageSource.getMessage("Service.RESOURCE_NOT_FOUND", null, LocaleContextHolder.getLocale())
            );
        }
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
            NoteUpdateRequestDTO noteUpdateRequestDTO
    ) {
        Book foundBook = getBookByUser(bookId, userId);
        if(
                noteUpdateRequestDTO.pageNumber() != null &&
                noteUpdateRequestDTO.pageNumber() > foundBook.getTotalPages()
        ) {
            throw new InvalidPageNumberException(
                    messageSource.getMessage("Service.PAGE_NUMBER_EXCEEDS_BOOK", null, LocaleContextHolder.getLocale())
            );
        };
        Note foundNote = getNoteByBookAndUser(noteId, bookId, userId);
        if(noteUpdateRequestDTO.pageNumber() != null) {
            foundNote.setPageNumber(noteUpdateRequestDTO.pageNumber());
        }
        if(noteUpdateRequestDTO.content() != null) {
            foundNote.setContent(noteUpdateRequestDTO.content());
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
        Note foundNote = getNoteByBookAndUser(
                noteId, bookId, userId
        );
        noteRepository.delete(foundNote);
    }

    // Utility functions
    Book getBookByUser(Integer bookId, Integer userId) {
        return bookRepository.findByBookIdAndUserUserId(bookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage("Service.RESOURCE_NOT_FOUND", null, LocaleContextHolder.getLocale())
                ));
    };

    Note getNoteByBookAndUser(Integer noteId, Integer bookId, Integer userId) {
        return noteRepository.findByNoteIdAndBookBookIdAndBookUserUserId(
                noteId,
                bookId,
                userId
        )
        .orElseThrow(() -> new ResourceNotFoundException(
                messageSource.getMessage("Service.RESOURCE_NOT_FOUND", null, LocaleContextHolder.getLocale())
        ));
    }

}
