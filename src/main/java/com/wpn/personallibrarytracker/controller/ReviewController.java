package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewCreateRequestDTO;
import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewResponseDTO;
import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewUpdateRequestDTO;
import com.wpn.personallibrarytracker.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books/{bookId}/review")
@Validated
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(
            ReviewService reviewService
    ) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> addReview(
            @AuthenticationPrincipal Integer userId,
            @PathVariable Integer bookId,
            @RequestBody @Valid ReviewCreateRequestDTO reviewCreateRequestDTO
    ) {
        System.out.println("In controller");
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
            @AuthenticationPrincipal Integer userId,
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
            @AuthenticationPrincipal Integer userId,
            @PathVariable Integer bookId,
            @RequestBody @Valid ReviewUpdateRequestDTO reviewUpdateRequestDTO
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
            @AuthenticationPrincipal Integer userId,
            @PathVariable Integer bookId
    ) {
        reviewService.deleteReview(bookId, userId);
        return ResponseEntity.noContent().build();
    }

}
