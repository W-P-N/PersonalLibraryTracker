package com.wpn.personallibrarytracker.repository;

import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    void findByUserUserId_shouldReturnBooks_whenUserHasBooks() {
        // Arrange
        User user = new User();
        user.setUserName("testuser");
        user.setEmail("testuser@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        Book book1 = new Book();
        book1.setTitle("Title 1");
        book1.setAuthor("Author 1");
        book1.setTotalPages(100);
        book1.setUser(savedUser);

        Book book2 = new Book();
        book2.setTitle("Title 2");
        book2.setAuthor("Author 2");
        book2.setTotalPages(200);
        book2.setUser(savedUser);

        bookRepository.save(book1);
        bookRepository.save(book2);

        // Act
        Page<Book> books = bookRepository.findByUserUserId(savedUser.getUserId(), PageRequest.of(0, 10));

        // Assert
        Assertions.assertEquals(2, books.getContent().size());
        Assertions.assertTrue(books.getContent().stream().anyMatch(b -> b.getTitle().equals("Title 1")));
        Assertions.assertTrue(books.getContent().stream().anyMatch(b -> b.getTitle().equals("Title 2")));
    }

    @Test
    void findByUserUserId_shouldReturnEmptyList_whenUserHasNoBooks() {
        // Arrange
        User user = new User();
        user.setUserName("testuser2");
        user.setEmail("testuser2@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        // Act
        Page<Book> books = bookRepository.findByUserUserId(savedUser.getUserId(), PageRequest.of(0, 10));

        // Assert
        Assertions.assertTrue(books.isEmpty());
    }

    @Test
    void findByBookIdAndUserId_shouldReturnBookEntity_whenUserExistsAndHasBookWithGivenId() {
        // Arrange
        User user = new User();
        user.setUserName("testuser3");
        user.setEmail("testuser3@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        Book book1 = new Book();
        book1.setTitle("Title 1");
        book1.setAuthor("Author 1");
        book1.setTotalPages(100);
        book1.setUser(savedUser);

        Book savedBook = bookRepository.save(book1);

        // Act
        Book foundBook = bookRepository.findByBookIdAndUserUserId(savedBook.getBookId(), savedUser.getUserId())
                .orElseThrow(() -> new RuntimeException("Unable to find book"));

        // Assert
        Assertions.assertNotNull(foundBook.getBookId());
        Assertions.assertEquals("Title 1", foundBook.getTitle());
        Assertions.assertEquals("Author 1", foundBook.getAuthor());
        Assertions.assertEquals(100, foundBook.getTotalPages());
        Assertions.assertEquals(savedUser.getUserId(), foundBook.getUser().getUserId());
    }
    @Test
    void existsByBookIdAndUserUserId_shouldReturnTrue_whenBookExistsForUser() {
        // Arrange
        User user = new User();
        user.setUserName("testuser4");
        user.setEmail("testuser4@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        Book book1 = new Book();
        book1.setTitle("Title 1");
        book1.setAuthor("Author 1");
        book1.setTotalPages(100);
        book1.setUser(savedUser);

        Book savedBook = bookRepository.save(book1);

        // Act
        boolean exists = bookRepository.existsByBookIdAndUserUserId(savedBook.getBookId(), savedUser.getUserId());

        // Assert
        Assertions.assertTrue(exists);
    }

    @Test
    void existsByBookIdAndUserUserId_shouldReturnFalse_whenBookDoesNotExistForUser() {
        // Arrange
        User user = new User();
        user.setUserName("testuser5");
        user.setEmail("testuser5@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        // Act
        boolean exists = bookRepository.existsByBookIdAndUserUserId(999, savedUser.getUserId());

        // Assert
        Assertions.assertFalse(exists);
    }

    @Test
    void countByUserUserId_shouldReturnCountOfBooks_whenUserHasBooks() {
        // Arrange
        User user = new User();
        user.setUserName("testuser6");
        user.setEmail("testuser6@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        Book book1 = new Book();
        book1.setTitle("Title 1");
        book1.setAuthor("Author 1");
        book1.setTotalPages(100);
        book1.setUser(savedUser);

        Book book2 = new Book();
        book2.setTitle("Title 2");
        book2.setAuthor("Author 2");
        book2.setTotalPages(200);
        book2.setUser(savedUser);

        bookRepository.save(book1);
        bookRepository.save(book2);

        // Act
        Long count = bookRepository.countByUserUserId(savedUser.getUserId());

        // Assert
        Assertions.assertEquals(2L, count);
    }

    @Test
    void countByUserUserId_shouldReturnZero_whenUserHasNoBooks() {
        // Arrange
        User user = new User();
        user.setUserName("testuser7");
        user.setEmail("testuser7@mail.com");
        user.setPassword("password");
        User savedUser = userRepository.save(user);

        // Act
        Long count = bookRepository.countByUserUserId(savedUser.getUserId());

        // Assert
        Assertions.assertEquals(0L, count);
    }
}
