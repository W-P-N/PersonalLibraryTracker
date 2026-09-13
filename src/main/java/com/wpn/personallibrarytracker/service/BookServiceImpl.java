package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.bookDTOs.*;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteResponseDTO;
import com.wpn.personallibrarytracker.dto.readingSessionDTOs.ReadingSessionResponseDTO;
import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewResponseDTO;
import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.User;
import com.wpn.personallibrarytracker.exceptions.ResourceNotFoundException;
import com.wpn.personallibrarytracker.repository.BookRepository;
import com.wpn.personallibrarytracker.repository.UserRepository;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service(value = "bookService")
public class BookServiceImpl implements BookService {
    private final UserRepository userRepository;
    private final Environment environment;
    private final BookRepository bookRepository;

    public BookServiceImpl(
            UserRepository userRepository,
            Environment environment,
            BookRepository bookRepository
    ) {
        this.userRepository = userRepository;
        this.environment = environment;
        this.bookRepository = bookRepository;
    }

    @Transactional
    @Override
    public BookResponseDTO addBook(Integer userId, BookRequestDTO bookRequestDTO) {
        // Check if book already exists - business decision pending
        User foundUser = getUser(userId);
        return createAndSaveBook(
                foundUser,
                bookRequestDTO.title(),
                bookRequestDTO.author(),
                bookRequestDTO.totalPages(),
                bookRequestDTO.isbn(),
                bookRequestDTO.coverUrl()
        );
    }

    @Transactional
    @Override
    public BookResponseDTO addBookFromSearch(
            Integer userId,
            BookFromSearchRequestDTO bookFromSearchRequestDTO
    ) {
        User foundUser = getUser(userId);
        return createAndSaveBook(
                foundUser,
                bookFromSearchRequestDTO.title(),
                bookFromSearchRequestDTO.author(),
                bookFromSearchRequestDTO.totalPages(),
                bookFromSearchRequestDTO.isbn(),
                bookFromSearchRequestDTO.coverUrl()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public Page<BookResponseDTO> getBooksByUser(
            Integer userId,
            Pageable pageable
    ) {
        if(!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(
                    environment.getProperty("Service.RESOURCE_NOT_FOUND")
            );
        }
        Page<Book> bookList = bookRepository.findByUserUserId(
                userId,
                pageable
        );
        return bookList
                .map(book ->
                    new BookResponseDTO(
                        book.getBookId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getIsbn(),
                        book.getCoverUrl(),
                        book.getTotalPages()
                    )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public BookDetailsResponseDTO getBookDetails(Integer userId, Integer bookId) {
        Book foundBook = getBookByUser(bookId, userId);
        ReviewResponseDTO reviewResponseDTO = foundBook.getReview() != null ?
                new ReviewResponseDTO(
                        foundBook.getReview().getContent(),
                        foundBook.getReview().getRating(),
                        foundBook.getReview().getCreatedAt()
                )
                :
                null;
        return new BookDetailsResponseDTO(
                foundBook.getBookId(),
                foundBook.getTitle(),
                foundBook.getAuthor(),
                foundBook.getTotalPages(),
                foundBook.getIsbn(),
                foundBook.getCoverUrl(),
                reviewResponseDTO
        );
    }

    @Override
    @Transactional
    public BookResponseDTO updateBook(Integer userId, Integer bookId, BookUpdateRequestDTO bookUpdateRequestDTO) {
        Book foundBook = getBookByUser(bookId, userId);
        if(bookUpdateRequestDTO.title() != null) {
            foundBook.setTitle(bookUpdateRequestDTO.title());
        }
        if(bookUpdateRequestDTO.author() != null) {
            foundBook.setAuthor(bookUpdateRequestDTO.author());
        }
        if(bookUpdateRequestDTO.isbn() != null) {
            foundBook.setIsbn(bookUpdateRequestDTO.isbn());
        }
        if(bookUpdateRequestDTO.totalPages() != null) {
            foundBook.setTotalPages(bookUpdateRequestDTO.totalPages());
        }
        if(bookUpdateRequestDTO.coverUrl() != null) {
            foundBook.setCoverUrl(bookUpdateRequestDTO.coverUrl());
        }
        bookRepository.save(foundBook);
        return new BookResponseDTO(
                foundBook.getBookId(),
                foundBook.getTitle(),
                foundBook.getAuthor(),
                foundBook.getIsbn(),
                foundBook.getCoverUrl(),
                foundBook.getTotalPages()
        );
    }

    @Override
    @Transactional
    public void deleteBook(Integer userId, Integer bookId) {
        Book foundBook = getBookByUser(bookId, userId);
        bookRepository.delete(foundBook);
    }
    // Utility functions
    User getUser(Integer userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException(environment.getProperty("Service.RESOURCE_NOT_FOUND"))
        );
    };

    Book getBookByUser(Integer bookId, Integer userId) {
        return bookRepository.findByBookIdAndUserUserId(bookId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        environment.getProperty("Service.RESOURCE_NOT_FOUND")
                ));
    };
    private BookResponseDTO createAndSaveBook(
            User user,
            String title,
            String author,
            Integer totalPages,
            String isbn,
            String coverUrl
    ) {
        Book newBook = new Book();
        newBook.setTitle(title);
        newBook.setAuthor(author);
        newBook.setTotalPages(totalPages);
        newBook.setIsbn(isbn);
        newBook.setCoverUrl(coverUrl);
        newBook.setUser(user);
        Book savedBook = bookRepository.save(newBook);
        return new BookResponseDTO(
                savedBook.getBookId(), savedBook.getTitle(), savedBook.getAuthor(),
                savedBook.getIsbn(), savedBook.getCoverUrl(), savedBook.getTotalPages()
        );
    }
}
