package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewCreateRequestDTO;
import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewResponseDTO;
import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewUpdateRequestDTO;
import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.Review;
import com.wpn.personallibrarytracker.exceptions.BookNotFoundForUserException;
import com.wpn.personallibrarytracker.exceptions.ReviewAlreadyExistsException;
import com.wpn.personallibrarytracker.exceptions.ReviewNotFoundForTheBookException;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.repository.BookRepository;
import com.wpn.personallibrarytracker.repository.ReviewRepository;
import com.wpn.personallibrarytracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceImplTest {
    @Mock
    UserRepository userRepository;
    @Mock
    BookRepository bookRepository;
    @Mock
    ReviewRepository reviewRepository;
    @Mock
    Environment environment;
    @InjectMocks
    ReviewServiceImpl reviewService;

    // --- Create Review Tests ---
    @Test
    void addReview_happyPath_shouldReturnReviewResponseDTO() {
        ReviewCreateRequestDTO request = new ReviewCreateRequestDTO("Great book", 5);
        Book book = new Book();
        book.setBookId(1);
        Review review = new Review();
        review.setContent("Great book");
        review.setRating(5);
        review.setCreatedAt(LocalDateTime.now());
        
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.findByBookIdAndUserUserId(1, 1)).thenReturn(Optional.of(book));
        when(reviewRepository.existsByBookBookId(1)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponseDTO response = reviewService.addReview(1, 1, request);

        assertNotNull(response);
        assertEquals("Great book", response.content());
        assertEquals(5, response.rating());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void addReview_unHappyPath_shouldThrowUserNotExistsException() {
        ReviewCreateRequestDTO request = new ReviewCreateRequestDTO("Great book", 5);
        when(userRepository.existsById(1)).thenReturn(false);
        when(environment.getProperty("Service.USER_NOT_FOUND")).thenReturn("User not found");

        assertThrows(UserNotFoundException.class, () -> reviewService.addReview(1, 1, request));
    }

    @Test
    void addReview_unHappyPath_shouldThrowBookNotExistsForUserException() {
        ReviewCreateRequestDTO request = new ReviewCreateRequestDTO("Great book", 5);
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.findByBookIdAndUserUserId(1, 1)).thenReturn(Optional.empty());
        when(environment.getProperty("Service.BOOK_NOT_FOUND_FOR_USER")).thenReturn("Book not found");

        assertThrows(BookNotFoundForUserException.class, () -> reviewService.addReview(1, 1, request));
    }

    @Test
    void addReview_unHappyPath_shouldThrowReviewAlreadyExistsException() {
        ReviewCreateRequestDTO request = new ReviewCreateRequestDTO("Great book", 5);
        Book book = new Book();
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.findByBookIdAndUserUserId(1, 1)).thenReturn(Optional.of(book));
        when(reviewRepository.existsByBookBookId(1)).thenReturn(true);
        when(environment.getProperty("Service.REVIEW_ALREADY_EXISTS")).thenReturn("Review exists");

        assertThrows(ReviewAlreadyExistsException.class, () -> reviewService.addReview(1, 1, request));
    }

    // --- Get Review Tests ---
    @Test
    void getReview_happyPath_shouldReturnReviewResponseDTO() {
        Review review = new Review();
        review.setContent("Nice");
        review.setRating(4);
        review.setCreatedAt(LocalDateTime.now());
        
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(true);
        when(reviewRepository.findByBookBookIdAndBookUserUserId(1, 1)).thenReturn(Optional.of(review));
        
        ReviewResponseDTO response = reviewService.getReview(1, 1);
        
        assertNotNull(response);
        assertEquals("Nice", response.content());
        assertEquals(4, response.rating());
    }

    @Test
    void getReview_unHappyPath_shouldThrowUserNotFoundException() {
        when(userRepository.existsById(1)).thenReturn(false);
        when(environment.getProperty("Service.USER_NOT_FOUND")).thenReturn("User not found");

        assertThrows(UserNotFoundException.class, () -> reviewService.getReview(1, 1));
    }

    @Test
    void getReview_unHappyPath_shouldThrowBookNotFoundForUserException() {
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(false);
        when(environment.getProperty("Service.BOOK_NOT_FOUND_FOR_USER")).thenReturn("Book not found");

        assertThrows(BookNotFoundForUserException.class, () -> reviewService.getReview(1, 1));
    }

    @Test
    void getReview_unHappyPath_shouldThrowReviewNotFoundForTheBookException() {
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(true);
        when(reviewRepository.findByBookBookIdAndBookUserUserId(1, 1)).thenReturn(Optional.empty());
        when(environment.getProperty("Service.REVIEW_NOT_FOUND_FOR_BOOK")).thenReturn("Review not found");

        assertThrows(ReviewNotFoundForTheBookException.class, () -> reviewService.getReview(1, 1));
    }

    // --- Update Review Tests ---
    @Test
    void updateReview_happyPath_shouldReturnUpdatedReviewResponseDTO() {
        ReviewUpdateRequestDTO request = new ReviewUpdateRequestDTO("Updated", 3);
        Review review = new Review();
        review.setContent("Old");
        review.setRating(1);
        
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(true);
        when(reviewRepository.findByBookBookIdAndBookUserUserId(1, 1)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        
        ReviewResponseDTO response = reviewService.updateReview(1, 1, request);
        
        assertNotNull(response);
        assertEquals("Updated", response.content());
        assertEquals(3, response.rating());
    }

    @Test
    void updateReview_unHappyPath_shouldThrowUserNotFoundException() {
        ReviewUpdateRequestDTO request = new ReviewUpdateRequestDTO("Updated", 3);
        when(userRepository.existsById(1)).thenReturn(false);
        when(environment.getProperty("Service.USER_NOT_FOUND")).thenReturn("User not found");

        assertThrows(UserNotFoundException.class, () -> reviewService.updateReview(1, 1, request));
    }

    @Test
    void updateReview_unHappyPath_shouldThrowBookNotFoundForUserException() {
        ReviewUpdateRequestDTO request = new ReviewUpdateRequestDTO("Updated", 3);
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(false);
        when(environment.getProperty("Service.BOOK_NOT_FOUND_FOR_USER")).thenReturn("Book not found");

        assertThrows(BookNotFoundForUserException.class, () -> reviewService.updateReview(1, 1, request));
    }

    @Test
    void updateReview_unHappyPath_shouldThrowReviewNotFoundForTheBookException() {
        ReviewUpdateRequestDTO request = new ReviewUpdateRequestDTO("Updated", 3);
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(true);
        when(reviewRepository.findByBookBookIdAndBookUserUserId(1, 1)).thenReturn(Optional.empty());
        when(environment.getProperty("Service.REVIEW_NOT_FOUND_FOR_BOOK")).thenReturn("Review not found");

        assertThrows(ReviewNotFoundForTheBookException.class, () -> reviewService.updateReview(1, 1, request));
    }

    // --- Delete Review Tests ---
    @Test
    void deleteReview_happyPath_shouldDeleteReview() {
        Review review = new Review();
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(true);
        when(reviewRepository.findByBookBookIdAndBookUserUserId(1, 1)).thenReturn(Optional.of(review));
        
        reviewService.deleteReview(1, 1);
        
        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    void deleteReview_unHappyPath_shouldThrowUserNotFoundException() {
        when(userRepository.existsById(1)).thenReturn(false);
        when(environment.getProperty("Service.USER_NOT_FOUND")).thenReturn("User not found");

        assertThrows(UserNotFoundException.class, () -> reviewService.deleteReview(1, 1));
    }

    @Test
    void deleteReview_unHappyPath_shouldThrowBookNotFoundForUserException() {
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(false);
        when(environment.getProperty("Service.BOOK_NOT_FOUND_FOR_USER")).thenReturn("Book not found");

        assertThrows(BookNotFoundForUserException.class, () -> reviewService.deleteReview(1, 1));
    }

    @Test
    void deleteReview_unHappyPath_shouldThrowReviewNotFoundForTheBookException() {
        when(userRepository.existsById(1)).thenReturn(true);
        when(bookRepository.existsByBookIdAndUserUserId(1, 1)).thenReturn(true);
        when(reviewRepository.findByBookBookIdAndBookUserUserId(1, 1)).thenReturn(Optional.empty());
        when(environment.getProperty("Service.REVIEW_NOT_FOUND_FOR_BOOK")).thenReturn("Review not found");

        assertThrows(ReviewNotFoundForTheBookException.class, () -> reviewService.deleteReview(1, 1));
    }
}
