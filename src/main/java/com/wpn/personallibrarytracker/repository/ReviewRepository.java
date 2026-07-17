package com.wpn.personallibrarytracker.repository;

import com.wpn.personallibrarytracker.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    boolean existsByBookBookId(Integer bookId);
    Optional<Review> findByBookBookIdAndBookUserUserId(
            Integer bookId, Integer userId);
}
