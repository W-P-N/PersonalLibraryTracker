package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.BookRequestDTO;
import com.wpn.personallibrarytracker.dto.BookResponseDTO;
import com.wpn.personallibrarytracker.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
}
