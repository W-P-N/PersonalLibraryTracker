package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.BookRequestDTO;
import com.wpn.personallibrarytracker.dto.BookResponseDTO;

import java.util.List;

public interface BookService {
    BookResponseDTO addBook(Integer userId, BookRequestDTO bookRequestDTO);
    List<BookResponseDTO> getBooksByUser(Integer userId);
}
