package com.wpn.personallibrarytracker.repository;

import com.wpn.personallibrarytracker.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Integer> {
    List<Book> findByUserUserId(Integer userId);
}
