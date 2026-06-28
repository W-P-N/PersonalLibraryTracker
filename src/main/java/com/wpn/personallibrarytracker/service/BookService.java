package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.BookRequestDTO;
import com.wpn.personallibrarytracker.dto.BookResponseDTO;

public interface BookService {
    BookResponseDTO addBook(Integer userId, BookRequestDTO bookRequestDTO);

}
