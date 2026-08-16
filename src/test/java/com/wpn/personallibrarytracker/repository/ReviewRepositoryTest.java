package com.wpn.personallibrarytracker.repository;

import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.Review;
import com.wpn.personallibrarytracker.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ReviewRepositoryTest {
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void existsByBookBookId_happyPath_shouldReturnTrue() {
        User foundUser = createUser("testuser", "testuser@mail.com", "password");
        Book foundBook = createBook("Title", "Author", 100, foundUser);
        Review foundReview = createReview("Great book", 4, foundBook);

        boolean exists = reviewRepository.existsByBookBookId(foundBook.getBookId());

        assertTrue(exists);
    }

    @Test
    void existsByBookBookId_unHappyPath_shouldReturnFalse() {
        User foundUser = createUser("testuser2", "testuser2@mail.com", "password");
        Book foundBook = createBook("Title", "Author", 100, foundUser);

        boolean exists = reviewRepository.existsByBookBookId(foundBook.getBookId());

        assertFalse(exists);
    }

    @Test
    void findByBookBookIdAndBookUserUserId_happyPath_shouldReturnOptionalReview() {
        User foundUser = createUser("testuser3", "testuser3@mail.com", "password");
        Book foundBook = createBook("Title", "Author", 100, foundUser);
        Review foundReview = createReview("Great book", 4, foundBook);

        Optional<Review> foundReviewOptional = reviewRepository
                .findByBookBookIdAndBookUserUserId(
                        foundBook.getBookId(),
                        foundUser.getUserId()
                );

        assertTrue(foundReviewOptional.isPresent());
        assertEquals(foundReview.getReviewId(), foundReviewOptional.get().getReviewId());
    }

    @Test
    void findByBookBookIdAndBookUserUserId_unHappyPath_shouldReturnEmptyOptionalReview() {
        User foundUser = createUser("testuser4", "testuser4@mail.com", "password");
        Book foundBook = createBook("Title", "Author", 100, foundUser);

        Optional<Review> foundReviewOptional = reviewRepository
                .findByBookBookIdAndBookUserUserId(
                        foundBook.getBookId(),
                        foundUser.getUserId()
                );

        assertEquals(Optional.empty(), foundReviewOptional);
    }

    @Test
    void findAverageRatingByUserId_happyPath_shouldReturnAverageRating() {
        User foundUser = createUser("testuser5", "testuser5@mail.com", "password");

        Book foundBook1 = createBook("Title 1", "Author 1", 100, foundUser);
        createReview("Great book", 4, foundBook1);

        Book foundBook2 = createBook("Title 2", "Author 2", 200, foundUser);
        createReview("Excellent book", 5, foundBook2);

        Double averageRating = reviewRepository.findAverageRatingByUserId(foundUser.getUserId());

        assertEquals(4.5, averageRating);
    }

    @Test
    void findAverageRatingByUserId_unHappyPath_shouldReturnNull() {
        User foundUser = createUser("testuser6", "testuser6@mail.com", "password");

        Double averageRating = reviewRepository.findAverageRatingByUserId(foundUser.getUserId());

        assertNull(averageRating);
    }

    private User createUser(String userName, String email, String password) {
        User user = new User();
        user.setUserName(userName);
        user.setEmail(email);
        user.setPassword(password);
        return testEntityManager.persist(user);
    }

    private Book createBook(String title, String author, Integer totalPages, User user) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setTotalPages(totalPages);
        book.setUser(user);
        return testEntityManager.persist(book);
    }

    private Review createReview(String content, Integer rating, Book book) {
        Review review = new Review();
        review.setContent(content);
        review.setRating(rating);
        review.setCreatedAt(LocalDateTime.now());
        review.setBook(book);
        return testEntityManager.persist(review);
    }
}
