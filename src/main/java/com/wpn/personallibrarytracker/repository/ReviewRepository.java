package com.wpn.personallibrarytracker.repository;

import com.wpn.personallibrarytracker.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    boolean existsByBookBookId(Integer bookId);
    Optional<Review> findByBookBookIdAndBookUserUserId(
            Integer bookId, Integer userId);
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book.user.userId = :userId")
    Double findAverageRatingByUserId(@Param("userId") Integer userId);
}
