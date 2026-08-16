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
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service("reviewService")
public class ReviewServiceImpl implements ReviewService{
    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private  final Environment environment;

    public ReviewServiceImpl(
            ReviewRepository reviewRepository,
            BookRepository bookRepository,
            Environment environment
    ) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.environment = environment;
    }

    @Override
    @Transactional
    public ReviewResponseDTO addReview(
            Integer bookId,
            Integer userId,
            ReviewCreateRequestDTO reviewCreateRequestDTO
    ) {
        // Book Exists
        Book foundBook = getBookByUser(bookId, userId);
        // Review Already Exists for the book
        validateReviewNotAlreadyExists(bookId);
        // Create Review
        Review newReview = new Review();
        newReview.setBook(foundBook);
        newReview.setContent(reviewCreateRequestDTO.content());
        newReview.setCreatedAt(LocalDateTime.now());
        newReview.setRating(reviewCreateRequestDTO.rating());
        // Save to DB
        Review savedReview = reviewRepository.save(newReview);
        // Return ResponseDTO
        return new ReviewResponseDTO(
                savedReview.getContent(),
                savedReview.getRating(),
                savedReview.getCreatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDTO getReview(
            Integer bookId,
            Integer userId
    ) {
        // Get Review
        Review foundReview = reviewRepository
                .findByBookBookIdAndBookUserUserId(bookId, userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                environment.getProperty("Service.RESOURCE_NOT_FOUND")
                        )
                );
        // Return DTO
        return new ReviewResponseDTO(
                foundReview.getContent(),
                foundReview.getRating(),
                foundReview.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public ReviewResponseDTO updateReview(
            Integer bookId,
            Integer userId,
            ReviewUpdateRequestDTO reviewUpdateRequestDTO
    ) {
        // Get Review
        Review foundReview = reviewRepository.findByBookBookIdAndBookUserUserId(bookId, userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                environment.getProperty("Service.RESOURCE_NOT_FOUND")
                        )
                );
        // Update Review
        if(reviewUpdateRequestDTO.content() != null) {
            foundReview.setContent(
                    reviewUpdateRequestDTO.content()
            );
        }
        if(reviewUpdateRequestDTO.rating() != null) {
            foundReview.setRating(
                    reviewUpdateRequestDTO.rating()
            );
        }
        // Save Updated Review
        Review savedReview = reviewRepository.save(foundReview);
        // Return DTO
        return new ReviewResponseDTO(
                savedReview.getContent(),
                savedReview.getRating(),
                savedReview.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public void deleteReview(Integer bookId, Integer userId) {
        // Get Review
        Review foundReview = reviewRepository.findByBookBookIdAndBookUserUserId(bookId, userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                environment.getProperty("Service.RESOURCE_NOT_FOUND")
                        )
                );
        // Delete Review
        reviewRepository.delete(foundReview);
    }

    // Utility methods
    void validateReviewNotAlreadyExists(Integer bookId) {
        if(reviewRepository.existsByBookBookId(bookId)) {
            throw new ReviewAlreadyExistsException(
                    environment.getProperty("Service.REVIEW_ALREADY_EXISTS")
            );
        }
    }

    Book getBookByUser(Integer bookId, Integer userId) {
        return bookRepository.findByBookIdAndUserUserId(bookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        environment.getProperty("Service.RESOURCE_NOT_FOUND")
                ));
    };
}
