package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.BookDetailsResponseDTO;
import com.wpn.personallibrarytracker.dto.BookRequestDTO;
import com.wpn.personallibrarytracker.dto.BookResponseDTO;
import com.wpn.personallibrarytracker.dto.BookUpdateRequestDTO;
import com.wpn.personallibrarytracker.entity.Book;

import java.util.List;

public interface BookService {
    BookResponseDTO addBook(Integer userId, BookRequestDTO bookRequestDTO);
    List<BookResponseDTO> getBooksByUser(Integer userId);
    BookDetailsResponseDTO getBookByUser(Integer userId, Integer bookId);
    BookResponseDTO updateBook(Integer useId, Integer bookId, BookUpdateRequestDTO bookUpdateRequestDTO);
    void deleteBook(Integer userId, Integer bookId);
}
