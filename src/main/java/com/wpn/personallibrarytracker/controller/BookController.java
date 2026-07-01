package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.BookDetailsResponseDTO;
import com.wpn.personallibrarytracker.dto.BookRequestDTO;
import com.wpn.personallibrarytracker.dto.BookResponseDTO;
import com.wpn.personallibrarytracker.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
@Validated
public class BookController {
    @Autowired
    private BookService bookService;

    @PostMapping("/{userId}")
    public ResponseEntity<BookResponseDTO> addBook(
            @PathVariable Integer userId,
            @RequestBody BookRequestDTO bookRequestDTO
            ) {
        BookResponseDTO bookResponseDTO = bookService.addBook(userId, bookRequestDTO);
        return ResponseEntity.ok(bookResponseDTO);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<BookResponseDTO>> getBooksByUserId(
            @PathVariable Integer userId
    ) {
        List<BookResponseDTO> booksList = bookService.getBooksByUser(userId);
        return ResponseEntity.ok(booksList);
    }

    @GetMapping("/{userId}/{bookId}")
    public ResponseEntity<BookDetailsResponseDTO> getBookByBookIdUserId(
            @PathVariable Integer userId,
            @PathVariable Integer bookId
    ) {
        return ResponseEntity.ok(bookService.getBookByUser(userId, bookId));
    }
}
