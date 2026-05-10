package com.wpn.personallibrarytracker.repository;

import com.wpn.personallibrarytracker.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
}
