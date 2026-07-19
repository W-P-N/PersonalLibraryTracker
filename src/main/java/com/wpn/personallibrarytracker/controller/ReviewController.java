package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewCreateRequestDTO;
import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewResponseDTO;
import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewUpdateRequestDTO;
import com.wpn.personallibrarytracker.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}/books/{bookId}/review")
@Validated
public class ReviewController {
    @Autowired
    ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> addReview(
            @PathVariable Integer userId,
            @PathVariable Integer bookId,
            @RequestBody ReviewCreateRequestDTO reviewCreateRequestDTO
    ) {
        return new ResponseEntity<>(
                reviewService.addReview(
                        bookId,
                        userId,
                        reviewCreateRequestDTO
                ),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ReviewResponseDTO> getReview(
            @PathVariable Integer userId,
            @PathVariable Integer bookId
    ) {
        return ResponseEntity.ok(
                reviewService.getReview(
                        bookId,
                        userId
                )
        );
    }

    @PatchMapping
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Integer userId,
            @PathVariable Integer bookId,
            @RequestBody ReviewUpdateRequestDTO reviewUpdateRequestDTO
    ) {
        return ResponseEntity.ok(
                reviewService.updateReview(
                        bookId,
                        userId,
                        reviewUpdateRequestDTO
                )
        );
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteReview(
            @PathVariable Integer userId,
            @PathVariable Integer bookId
    ) {
        reviewService.deleteReview(bookId, userId);
        return ResponseEntity.noContent().build();
    }

}
