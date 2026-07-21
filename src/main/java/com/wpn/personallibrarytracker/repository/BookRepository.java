package com.wpn.personallibrarytracker.repository;

import com.wpn.personallibrarytracker.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {
    Page<Book> findByUserUserId(Integer userId, Pageable pageable);
    Optional<Book> findByBookIdAndUserUserId(Integer bookId, Integer userId);
    boolean existsByBookIdAndUserUserId(Integer bookId, Integer userId);
    Long countByUserUserId(Integer userId);
}
