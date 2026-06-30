package com.wpn.personallibrarytracker.repository;

import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

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
        List<Book> books = bookRepository.findByUserUserId(savedUser.getUserId());

        // Assert
        Assertions.assertEquals(2, books.size());
        Assertions.assertTrue(books.stream().anyMatch(b -> b.getTitle().equals("Title 1")));
        Assertions.assertTrue(books.stream().anyMatch(b -> b.getTitle().equals("Title 2")));
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
        List<Book> books = bookRepository.findByUserUserId(savedUser.getUserId());

        // Assert
        Assertions.assertTrue(books.isEmpty());
    }
}
