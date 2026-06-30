package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.BookRequestDTO;
import com.wpn.personallibrarytracker.dto.BookResponseDTO;
import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.User;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.repository.BookRepository;
import com.wpn.personallibrarytracker.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service(value = "bookService")
public class BookServiceImpl implements BookService {
    private final UserRepository userRepository;
    private final Environment environment;
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    public BookServiceImpl(
            UserRepository userRepository,
            Environment environment,
            BookRepository bookRepository,
            ModelMapper modelMapper
    ) {
        this.userRepository = userRepository;
        this.environment = environment;
        this.bookRepository = bookRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @Override
    public BookResponseDTO addBook(Integer userId, BookRequestDTO bookRequestDTO) {
        User foundUser = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(environment.getProperty("Service.USER_NOT_FOUND"))
        );
        Book newBook = new Book();
        newBook.setAuthor(bookRequestDTO.author());
        newBook.setTitle(bookRequestDTO.title());
        newBook.setIsbn(bookRequestDTO.isbn());
        newBook.setTotalPages(bookRequestDTO.totalPages());
        newBook.setCoverUrl(bookRequestDTO.coverUrl());

        Book savedBook = bookRepository.save(newBook);

        if(foundUser.getBooks() == null) {
            foundUser.setBooks(new ArrayList<>());
        }
        foundUser.getBooks().add(savedBook);

        return new BookResponseDTO(
                savedBook.getBookId(),
                savedBook.getTitle(),
                savedBook.getAuthor(),
                savedBook.getIsbn(),
                savedBook.getCoverUrl(),
                savedBook.getTotalPages()
        );
    }

    @Override
    public List<BookResponseDTO> getBooksByUser(Integer userId) {
        User foundUser = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(
                environment.getProperty("Service.USER_NOT_FOUND")
        ));
        List<Book> bookList = bookRepository.findByUserUserId(foundUser.getUserId());
        if(bookList.isEmpty()) {
            return List.of();
        }
        return bookList.stream()
                .map(book ->
                    new BookResponseDTO(
                        book.getBookId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getIsbn(),
                        book.getCoverUrl(),
                        book.getTotalPages()
                    )
                )
                .toList();
    }
}
