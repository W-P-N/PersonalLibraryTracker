package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewCreateRequestDTO;
import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewResponseDTO;
import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewUpdateRequestDTO;

public interface ReviewService {
    ReviewResponseDTO addReview(Integer bookId, Integer userId, ReviewCreateRequestDTO reviewCreateRequestDTO);
    ReviewResponseDTO getReview(Integer bookId, Integer userId);
    ReviewResponseDTO updateReview(Integer bookId, Integer userId, ReviewUpdateRequestDTO reviewUpdateRequestDTO);
    void deleteReview(Integer bookId, Integer userId);
}
