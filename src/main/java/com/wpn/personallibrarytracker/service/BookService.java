package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.bookDTOs.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {
    BookResponseDTO addBook(Integer userId, BookRequestDTO bookRequestDTO);
    BookResponseDTO addBookFromSearch(Integer userId, BookFromSearchRequestDTO bookFromSearchRequestDTO);
    Page<BookResponseDTO> getBooksByUser(Integer userId, Pageable pageable);
    BookDetailsResponseDTO getBookDetails(Integer userId, Integer bookId);
    BookResponseDTO updateBook(Integer userId, Integer bookId, BookUpdateRequestDTO bookUpdateRequestDTO);
    void deleteBook(Integer userId, Integer bookId);
}
