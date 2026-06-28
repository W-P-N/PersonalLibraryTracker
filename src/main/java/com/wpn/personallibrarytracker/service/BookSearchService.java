package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.BookSearchResponseDTO;

import java.util.List;

public interface BookSearchService {
    List<BookSearchResponseDTO> searchBooksByTerm(String searchTerm);
    BookSearchResponseDTO searchBookByIsbn(String isbn);
}
