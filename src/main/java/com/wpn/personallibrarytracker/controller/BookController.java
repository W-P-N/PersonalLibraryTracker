package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.bookDTOs.*;
import com.wpn.personallibrarytracker.exceptions.BookNotFoundForUserException;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.service.BookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
            @RequestBody @Valid BookRequestDTO bookRequestDTO
            ) {
        BookResponseDTO bookResponseDTO = bookService.addBook(userId, bookRequestDTO);
        return new ResponseEntity<>(
                bookResponseDTO,
                HttpStatus.CREATED
        );
    }

    @PostMapping("/from-search")
    public ResponseEntity<BookResponseDTO> addBookFromSearch(
            @PathVariable Integer userId,
            @RequestBody @Valid BookFromSearchRequestDTO bookFromSearchRequestDTO
    ) {
        BookResponseDTO bookResponseDTO = bookService.addBookFromSearch(userId, bookFromSearchRequestDTO);
        return new ResponseEntity<>(
                bookResponseDTO,
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<Page<BookResponseDTO>> getBooks(
            @PathVariable Integer userId,
            @RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "5") Integer pageSize
    ) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<BookResponseDTO> booksList = bookService.getBooksByUser(userId, pageable);
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
