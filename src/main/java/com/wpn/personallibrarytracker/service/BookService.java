package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.bookDTOs.BookDetailsResponseDTO;
import com.wpn.personallibrarytracker.dto.bookDTOs.BookRequestDTO;
import com.wpn.personallibrarytracker.dto.bookDTOs.BookResponseDTO;
import com.wpn.personallibrarytracker.dto.bookDTOs.BookUpdateRequestDTO;

import java.util.List;

public interface BookService {
    BookResponseDTO addBook(Integer userId, BookRequestDTO bookRequestDTO);
    List<BookResponseDTO> getBooksByUser(Integer userId);
    BookDetailsResponseDTO getBookDetails(Integer userId, Integer bookId);
    BookResponseDTO updateBook(Integer userId, Integer bookId, BookUpdateRequestDTO bookUpdateRequestDTO);
    void deleteBook(Integer userId, Integer bookId);
}
