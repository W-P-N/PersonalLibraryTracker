package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.bookDTOs.BookDetailsResponseDTO;
import com.wpn.personallibrarytracker.dto.bookDTOs.BookRequestDTO;
import com.wpn.personallibrarytracker.dto.bookDTOs.BookResponseDTO;
import com.wpn.personallibrarytracker.dto.bookDTOs.BookUpdateRequestDTO;
import com.wpn.personallibrarytracker.exceptions.BookNotFoundForUserException;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/books")
@Validated
public class BookController {
    @Autowired
    private BookService bookService;

    @PostMapping
    public ResponseEntity<BookResponseDTO> addBook(
            @PathVariable Integer userId,
            @RequestBody BookRequestDTO bookRequestDTO
            ) {
        BookResponseDTO bookResponseDTO = bookService.addBook(userId, bookRequestDTO);
        return ResponseEntity.ok(bookResponseDTO);
    }

    @GetMapping
    public ResponseEntity<List<BookResponseDTO>> getBooks(
            @PathVariable Integer userId
    ) {
        List<BookResponseDTO> booksList = bookService.getBooksByUser(userId);
        return ResponseEntity.ok(booksList);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookDetailsResponseDTO> getBookById(
            @PathVariable Integer userId,
            @PathVariable Integer bookId
    ) {
        return ResponseEntity.ok(bookService.getBookDetails(userId, bookId));
    }

    @PatchMapping("/{bookId}")
    public ResponseEntity<BookResponseDTO> updateBook(
            @PathVariable Integer userId,
            @PathVariable Integer bookId,
            @RequestBody
            @Valid
            BookUpdateRequestDTO bookUpdateRequestDTO
    ) {
        return ResponseEntity.ok(bookService.updateBook(userId, bookId, bookUpdateRequestDTO));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable
            Integer userId,
            @PathVariable
            Integer bookId
    ) throws UserNotFoundException, BookNotFoundForUserException {
        bookService.deleteBook(userId, bookId);
        return ResponseEntity.noContent().build();
    };
}
