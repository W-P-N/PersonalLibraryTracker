package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewCreateRequestDTO;
import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewResponseDTO;
import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewUpdateRequestDTO;
import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.Review;
import com.wpn.personallibrarytracker.exceptions.ResourceNotFoundException;
import com.wpn.personallibrarytracker.exceptions.ReviewAlreadyExistsException;
import com.wpn.personallibrarytracker.repository.BookRepository;
import com.wpn.personallibrarytracker.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.mockito.ArgumentMatchers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceImplTest {
    @Mock
    BookRepository bookRepository;
    @Mock
    ReviewRepository reviewRepository;
    @Mock
    MessageSource messageSource;
    @InjectMocks
    ReviewServiceImpl reviewService;

    @Test
    void addReview_happyPath_shouldReturnReviewResponseDTO() {
        ReviewCreateRequestDTO request = new ReviewCreateRequestDTO("Great book", 5);
        Book book = new Book();
        book.setBookId(1);
        Review review = new Review();
        review.setContent("Great book");
        review.setRating(5);
        review.setCreatedAt(LocalDateTime.now());

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
    void addReview_unHappyPath_shouldThrowResourceNotFoundException_whenBookNotFoundForUser() {
        ReviewCreateRequestDTO request = new ReviewCreateRequestDTO("Great book", 5);
        when(bookRepository.findByBookIdAndUserUserId(1, 1)).thenReturn(Optional.empty());
        when(messageSource.getMessage(ArgumentMatchers.eq("Service.RESOURCE_NOT_FOUND"), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn("The requested resource was not found");

        assertThrows(ResourceNotFoundException.class, () -> reviewService.addReview(1, 1, request));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void addReview_unHappyPath_shouldThrowReviewAlreadyExistsException() {
        ReviewCreateRequestDTO request = new ReviewCreateRequestDTO("Great book", 5);
        Book book = new Book();
        when(bookRepository.findByBookIdAndUserUserId(1, 1)).thenReturn(Optional.of(book));
        when(reviewRepository.existsByBookBookId(1)).thenReturn(true);
        when(messageSource.getMessage(ArgumentMatchers.eq("Service.REVIEW_ALREADY_EXISTS"), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn("Review exists");

        assertThrows(ReviewAlreadyExistsException.class, () -> reviewService.addReview(1, 1, request));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void getReview_happyPath_shouldReturnReviewResponseDTO() {
        Review review = new Review();
        review.setContent("Nice");
        review.setRating(4);
        review.setCreatedAt(LocalDateTime.now());

        when(reviewRepository.findByBookBookIdAndBookUserUserId(1, 1)).thenReturn(Optional.of(review));

        ReviewResponseDTO response = reviewService.getReview(1, 1);

        assertNotNull(response);
        assertEquals("Nice", response.content());
        assertEquals(4, response.rating());
        verify(bookRepository, never()).existsByBookIdAndUserUserId(any(), any());
    }

    @Test
    void getReview_unHappyPath_shouldThrowResourceNotFoundException_whenReviewNotFound() {
        when(reviewRepository.findByBookBookIdAndBookUserUserId(1, 1)).thenReturn(Optional.empty());
        when(messageSource.getMessage(ArgumentMatchers.eq("Service.RESOURCE_NOT_FOUND"), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn("The requested resource was not found");

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReview(1, 1));
    }

    @Test
    void updateReview_happyPath_shouldReturnUpdatedReviewResponseDTO() {
        ReviewUpdateRequestDTO request = new ReviewUpdateRequestDTO("Updated", 3);
        Review review = new Review();
        review.setContent("Old");
        review.setRating(1);
        review.setCreatedAt(LocalDateTime.now());

        when(reviewRepository.findByBookBookIdAndBookUserUserId(1, 1)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponseDTO response = reviewService.updateReview(1, 1, request);

        assertNotNull(response);
        assertEquals("Updated", response.content());
        assertEquals(3, response.rating());
        verify(bookRepository, never()).existsByBookIdAndUserUserId(any(), any());
    }

    @Test
    void updateReview_unHappyPath_shouldThrowResourceNotFoundException_whenReviewNotFound() {
        ReviewUpdateRequestDTO request = new ReviewUpdateRequestDTO("Updated", 3);
        when(reviewRepository.findByBookBookIdAndBookUserUserId(1, 1)).thenReturn(Optional.empty());
        when(messageSource.getMessage(ArgumentMatchers.eq("Service.RESOURCE_NOT_FOUND"), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn("The requested resource was not found");

        assertThrows(ResourceNotFoundException.class, () -> reviewService.updateReview(1, 1, request));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void deleteReview_happyPath_shouldDeleteReview() {
        Review review = new Review();
        when(reviewRepository.findByBookBookIdAndBookUserUserId(1, 1)).thenReturn(Optional.of(review));

        reviewService.deleteReview(1, 1);

        verify(reviewRepository, times(1)).delete(review);
        verify(bookRepository, never()).existsByBookIdAndUserUserId(any(), any());
    }

    @Test
    void deleteReview_unHappyPath_shouldThrowResourceNotFoundException_whenReviewNotFound() {
        when(reviewRepository.findByBookBookIdAndBookUserUserId(1, 1)).thenReturn(Optional.empty());
        when(messageSource.getMessage(ArgumentMatchers.eq("Service.RESOURCE_NOT_FOUND"), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn("The requested resource was not found");

        assertThrows(ResourceNotFoundException.class, () -> reviewService.deleteReview(1, 1));
        verify(reviewRepository, never()).delete(any());
    }
}
