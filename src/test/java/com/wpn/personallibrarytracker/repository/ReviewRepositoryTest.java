package com.wpn.personallibrarytracker.repository;

import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.Review;
import com.wpn.personallibrarytracker.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.core.env.Environment;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ReviewRepositoryTest {
    @Autowired
    private Environment environment;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void existsByBookBookId_happyPath_shouldReturnTrue() {
        // Arrange
        User foundUser = new User();
        testEntityManager.persist(foundUser);
        Book foundBook = new Book();
        foundBook.setUser(foundUser);
        testEntityManager.persist(foundBook);
        Review foundReview = new Review();
        foundReview.setBook(foundBook);
        testEntityManager.persist(foundReview);

        // Act
        boolean exists = reviewRepository
                .existsByBookBookId(foundBook.getBookId());

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByBookBookId_unHappyPath_shouldReturnFalse() {
        // Arrange
        User foundUser = new User();
        testEntityManager.persist(foundUser);
        Book foundBook = new Book();
        foundBook.setUser(foundUser);
        testEntityManager.persist(foundBook);

        // Act
        boolean exists = reviewRepository
                .existsByBookBookId(foundBook.getBookId());

        // Assert
        assertFalse(exists);
    }

    @Test
    void findByBookBookIdAndBookUserUserId_happyPath_shouldReturnOptionalReview() {
        // Arrange
        User foundUser = new User();
        testEntityManager.persist(foundUser);
        Book foundBook = new Book();
        foundBook.setUser(foundUser);
        testEntityManager.persist(foundBook);
        Review foundReview = new Review();
        foundReview.setBook(foundBook);
        testEntityManager.persist(foundReview);

        // Act
        Optional<Review> foundReviewOptional = reviewRepository
                .findByBookBookIdAndBookUserUserId(
                        foundBook.getBookId(),
                        foundUser.getUserId()
                );

        // Assert
        assertNotNull(foundReviewOptional);
        Review foundReviewContent = foundReviewOptional.get();
        assertEquals(foundReview.getReviewId(), foundReviewContent.getReviewId());
    }

    @Test
    void findByBookBookIdAndBookUserUserId_unHappyPath_shouldReturnEmptyOptionalReview() {
        // Arrange
        User foundUser = new User();
        testEntityManager.persist(foundUser);
        Book foundBook = new Book();
        foundBook.setUser(foundUser);
        testEntityManager.persist(foundBook);

        // Act
        Optional<Review> foundReviewOptional = reviewRepository
                .findByBookBookIdAndBookUserUserId(
                        foundBook.getBookId(),
                        foundUser.getUserId()
                );

        // Assert
        assertEquals(Optional.empty(), foundReviewOptional);
    }

    @Test
    void findAverageRatingByUserId_happyPath_shouldReturnAverageRating() {
        // Arrange
        User foundUser = new User();
        testEntityManager.persist(foundUser);
        
        Book foundBook1 = new Book();
        foundBook1.setUser(foundUser);
        testEntityManager.persist(foundBook1);
        Review foundReview1 = new Review();
        foundReview1.setBook(foundBook1);
        foundReview1.setRating(4);
        testEntityManager.persist(foundReview1);

        Book foundBook2 = new Book();
        foundBook2.setUser(foundUser);
        testEntityManager.persist(foundBook2);
        Review foundReview2 = new Review();
        foundReview2.setBook(foundBook2);
        foundReview2.setRating(5);
        testEntityManager.persist(foundReview2);

        // Act
        Double averageRating = reviewRepository.findAverageRatingByUserId(foundUser.getUserId());

        // Assert
        assertEquals(4.5, averageRating);
    }

    @Test
    void findAverageRatingByUserId_unHappyPath_shouldReturnNull() {
        // Arrange
        User foundUser = new User();
        testEntityManager.persist(foundUser);

        // Act
        Double averageRating = reviewRepository.findAverageRatingByUserId(foundUser.getUserId());

        // Assert
        assertNull(averageRating);
    }
}
