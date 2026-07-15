package com.wpn.personallibrarytracker.repository;

import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.Note;
import com.wpn.personallibrarytracker.entity.User;
import com.wpn.personallibrarytracker.exceptions.NoteNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@DataJpaTest
public class NoteRepositoryTest {
    @Autowired
    private Environment environment;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void findByBookBookIdAndBookUserUserId_shouldReturnPageOfNotes() {
        // Arrange
        // // User
        User newUser = createUser("test", "test@mail.com", "test@123");
        // // Book
        Book newBook = createBook("testBook", 4000, newUser);
        // // Note1
        Note note1 = createNote("test content 1", 3, newBook);
        // // Note2
        Note note2 = createNote("test content 2", 4, newBook);
        // // Pageable
        Pageable pageable = PageRequest.of(0, 5);
        // Act
        Page<Note> notePages = noteRepository.findByBookBookIdAndBookUserUserId(
                newBook.getBookId(),
                newUser.getUserId(),
                pageable
        );
        // Assert and Verify
        // // Metadata
        // // // Total elements
        Assertions.assertEquals(2, notePages.getTotalElements());
        // // // Total pages
        Assertions.assertEquals(1, notePages.getTotalPages());
        // // Content
        Assertions.assertEquals(2, notePages.getContent().size());
        Assertions.assertEquals(note1.getNoteId(), notePages.getContent().get(0).getNoteId());
        Assertions.assertEquals(note2.getNoteId(), notePages.getContent().get(1).getNoteId());
    }

    @Test
    void findByBookBookIdAndBookUserUserId_shouldReturnEmptyPageWhenOutOfRange() {
        // Arrange
        // // User
        User newUser = createUser("test", "test@mail.com", "test@123");
        // // Book
        Book newBook = createBook("testBook", 4000, newUser);
        // // Note1
        Note note1 = createNote("test content 1", 3, newBook);
        // // Note2
        Note note2 = createNote("test content 2", 4, newBook);
        // // Pageable
        Pageable pageable = PageRequest.of(10, 5);
        // Act
        Page<Note> notePages = noteRepository.findByBookBookIdAndBookUserUserId(
                newBook.getBookId(),
                newUser.getUserId(),
                pageable
        );
        // Assert and Verify
        // // Element
        Assertions.assertNotNull(notePages);
        Assertions.assertTrue(notePages.isEmpty());
        Assertions.assertEquals(0, notePages.getContent().size());
    }

    @Test
    void findByNoteIdAndBookBookIdAndBookUserUserId_shouldReturnNoteById() {
        // Arrange
        // // User
        User newUser = createUser("test", "test@mail.com", "test@123");
        // // Book
        Book newBook = createBook("testBook", 4000, newUser);
        // // Note1
        Note note1 = createNote("test content 1", 3, newBook);
        // Act
        Note foundNote = noteRepository.findByNoteIdAndBookBookIdAndBookUserUserId(
                note1.getNoteId(),
                newBook.getBookId(),
                newUser.getUserId()
        ).orElseThrow(
                () -> new NoteNotFoundException(
                        environment.getProperty("Service.NOTE_NOT_FOUND")
                )
        );
        // Assert and Verify
        Assertions.assertEquals(note1.getNoteId(), foundNote.getNoteId());
        Assertions.assertEquals(note1.getContent(), foundNote.getContent());
        Assertions.assertEquals(note1.getBook().getBookId(), foundNote.getBook().getBookId());
    }

    // Utility methods
    User createUser(String userName, String email, String password) {
        User newUser = new User();
        newUser.setUserName(userName);
        newUser.setEmail(email);
        newUser.setPassword(password);
        return testEntityManager.persist(newUser);
    };

    Book createBook(
            String title,
            Integer totalPages,
            User user
    ) {
        Book newBook = new Book();
        newBook.setTitle(title);
        newBook.setTotalPages(totalPages);
        newBook.setUser(user);
        return testEntityManager.persist(newBook);
    };

    Note createNote(
            String content,
            Integer pageNumber,
            Book book
    ) {
        Note newNote = new Note();
        newNote.setContent(content);
        newNote.setPageNumber(pageNumber);
        newNote.setBook(book);
        return testEntityManager.persist(newNote);
    };
}
