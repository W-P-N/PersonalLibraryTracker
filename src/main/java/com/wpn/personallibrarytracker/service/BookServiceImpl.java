package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.bookDTOs.BookDetailsResponseDTO;
import com.wpn.personallibrarytracker.dto.bookDTOs.BookRequestDTO;
import com.wpn.personallibrarytracker.dto.bookDTOs.BookResponseDTO;
import com.wpn.personallibrarytracker.dto.bookDTOs.BookUpdateRequestDTO;
import com.wpn.personallibrarytracker.dto.noteDTOs.NoteResponseDTO;
import com.wpn.personallibrarytracker.dto.readingSessionDTOs.ReadingSessionResponseDTO;
import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewResponseDTO;
import com.wpn.personallibrarytracker.entity.Book;
import com.wpn.personallibrarytracker.entity.User;
import com.wpn.personallibrarytracker.exceptions.BookNotFoundForUserException;
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
        newBook.setTitle(bookRequestDTO.title());
        newBook.setAuthor(bookRequestDTO.author());
        newBook.setIsbn(bookRequestDTO.isbn());
        newBook.setTotalPages(bookRequestDTO.totalPages());
        newBook.setCoverUrl(bookRequestDTO.coverUrl());
        newBook.setUser(foundUser);
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

    @Override
    @Transactional(readOnly = true)
    public BookDetailsResponseDTO getBookDetails(Integer userId, Integer bookId) {
        if(!userRepository.existsById(userId)) {
            throw new UserNotFoundException(
                    environment.getProperty("Service.USER_NOT_FOUND")
            );
        };
        Book foundBook = bookRepository.findByBookIdAndUserUserId(bookId, userId)
                .orElseThrow(() -> new BookNotFoundForUserException(
                        environment.getProperty("Service.BOOK_NOT_FOUND_FOR_USER")
                ));
        List<ReadingSessionResponseDTO> readingSessionResponseDTOList = foundBook.getReadingSessions()
                .stream().map(readingSession -> new ReadingSessionResponseDTO(
                        readingSession.getReadingSessionId(),
                        readingSession.getPagesReadInSession(),
                        readingSession.getEndSessionPageNumber(),
                        readingSession.getSessionDateTime()
                )).toList();
        List<NoteResponseDTO> noteResponseDTOList = foundBook.getNotes()
                .stream()
                .map(note -> new NoteResponseDTO(
                        note.getNoteId(),
                        note.getContent(),
                        note.getCreatedAt(),
                        note.getPageNumber()
                ))
                .toList();
        ReviewResponseDTO reviewResponseDTO = foundBook.getReview() != null ?
                new ReviewResponseDTO(
                        foundBook.getReview().getReviewId(),
                        foundBook.getReview().getContent(),
                        foundBook.getReview().getRating()
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
                readingSessionResponseDTOList,
                noteResponseDTOList,
                reviewResponseDTO
        );
    }

    @Override
    @Transactional
    public BookResponseDTO updateBook(Integer userId, Integer bookId, BookUpdateRequestDTO bookUpdateRequestDTO) {
        if(!userRepository.existsById(userId)) {
            throw new UserNotFoundException(
                    environment.getProperty("Service.USER_NOT_FOUND")
            );
        };
        Book foundBook = bookRepository.findByBookIdAndUserUserId(bookId, userId)
                .orElseThrow(() -> new BookNotFoundForUserException(
                        environment.getProperty("Service.BOOK_NOT_FOUND_FOR_USER")
                ));
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
        if(!userRepository.existsById(userId)) {
            throw new UserNotFoundException(
                    environment.getProperty("Service.USER_NOT_FOUND")
            );
        };
        Book foundBook = bookRepository.findByBookIdAndUserUserId(bookId, userId)
                .orElseThrow(() -> new BookNotFoundForUserException(
                        environment.getProperty("Service.BOOK_NOT_FOUND_FOR_USER")
                ));
        bookRepository.delete(foundBook);
    }
}
