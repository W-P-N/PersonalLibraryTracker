package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.BookSearchResponseDTO;
import com.wpn.personallibrarytracker.service.BookSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books/search")
public class PublicBookController {
    @Autowired
    private BookSearchService bookSearchService;

    @GetMapping
    public ResponseEntity<List<BookSearchResponseDTO>> searchBooks(@RequestParam String query) {
        List<BookSearchResponseDTO> results = bookSearchService.searchBooksByTerm(query);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<BookSearchResponseDTO> searchByIsbn(@PathVariable String isbn) {
        BookSearchResponseDTO result = bookSearchService.searchBookByIsbn(isbn);
        return ResponseEntity.ok(result);
    }

}
