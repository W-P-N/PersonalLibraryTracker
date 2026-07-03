package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.bookDTOs.BookSearchResponseDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class BookSearchServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Environment environment;

    @InjectMocks
    private BookSearchServiceImpl bookSearchService;

    @Test
    public void searchBooksByTerm_shouldReturnMappedList_whenApiReturnsValidData() {
        // Arrange
        Mockito.when(environment.getProperty("BOOKS_API_KEY")).thenReturn("mock-api-key");

        BookSearchServiceImpl.IndustryIdentifier isbn10 = new BookSearchServiceImpl.IndustryIdentifier("ISBN_10", "1484465474");
        BookSearchServiceImpl.IndustryIdentifier isbn13 = new BookSearchServiceImpl.IndustryIdentifier("ISBN_13", "9781484465479");
        BookSearchServiceImpl.ImageLinks imageLinks = new BookSearchServiceImpl.ImageLinks(
                "http://books.google.com/small.jpg",
                "http://books.google.com/thumbnail.jpg"
        );
        BookSearchServiceImpl.VolumeInfo volumeInfo = new BookSearchServiceImpl.VolumeInfo(
                "Harry Potter and the Sorcerer's Stone",
                List.of("J. K. Rowling"),
                "A young boy with a great destiny...",
                List.of(isbn10, isbn13),
                309,
                imageLinks
        );
        BookSearchServiceImpl.GoogleBookItem item = new BookSearchServiceImpl.GoogleBookItem(volumeInfo);
        BookSearchServiceImpl.GoogleBooksResponse mockResponse = new BookSearchServiceImpl.GoogleBooksResponse(List.of(item));

        String expectedUrl = "https://www.googleapis.com/books/v1/volumes?q=harrypotter&key=mock-api-key";
        Mockito.when(restTemplate.getForObject(expectedUrl, BookSearchServiceImpl.GoogleBooksResponse.class))
                .thenReturn(mockResponse);

        // Act
        List<BookSearchResponseDTO> results = bookSearchService.searchBooksByTerm("harrypotter");

        // Assert
        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.size());

        BookSearchResponseDTO dto = results.get(0);
        Assertions.assertEquals("Harry Potter and the Sorcerer's Stone", dto.title());
        Assertions.assertEquals("J. K. Rowling", dto.author());
        Assertions.assertEquals("A young boy with a great destiny...", dto.description());
        Assertions.assertEquals("9781484465479", dto.isbn()); // Resolves to ISBN_13
        Assertions.assertEquals("https://books.google.com/thumbnail.jpg", dto.coverUrl()); // Replaced http with https
        Assertions.assertEquals(309, dto.totalPages());
    }

    @Test
    public void searchBooksByTerm_shouldReturnEmptyList_whenApiReturnsNull() {
        // Arrange
        Mockito.when(environment.getProperty("BOOKS_API_KEY")).thenReturn("mock-api-key");
        String expectedUrl = "https://www.googleapis.com/books/v1/volumes?q=unknownbook&key=mock-api-key";
        Mockito.when(restTemplate.getForObject(expectedUrl, BookSearchServiceImpl.GoogleBooksResponse.class))
                .thenReturn(null);

        // Act
        List<BookSearchResponseDTO> results = bookSearchService.searchBooksByTerm("unknownbook");

        // Assert
        Assertions.assertNotNull(results);
        Assertions.assertTrue(results.isEmpty());
    }

    @Test
    public void searchBookByIsbn_shouldReturnSingleBook_whenIsbnIsFound() {
        // Arrange
        Mockito.when(environment.getProperty("BOOKS_API_KEY")).thenReturn("mock-api-key");

        BookSearchServiceImpl.IndustryIdentifier isbn13 = new BookSearchServiceImpl.IndustryIdentifier("ISBN_13", "9781484465479");
        BookSearchServiceImpl.VolumeInfo volumeInfo = new BookSearchServiceImpl.VolumeInfo(
                "Harry Potter and the Sorcerer's Stone",
                List.of("J. K. Rowling"),
                "A young boy...",
                List.of(isbn13),
                309,
                null
        );
        BookSearchServiceImpl.GoogleBookItem item = new BookSearchServiceImpl.GoogleBookItem(volumeInfo);
        BookSearchServiceImpl.GoogleBooksResponse mockResponse = new BookSearchServiceImpl.GoogleBooksResponse(List.of(item));

        String expectedUrl = "https://www.googleapis.com/books/v1/volumes?q=isbn:9781484465479&key=mock-api-key";
        Mockito.when(restTemplate.getForObject(expectedUrl, BookSearchServiceImpl.GoogleBooksResponse.class))
                .thenReturn(mockResponse);

        // Act
        BookSearchResponseDTO result = bookSearchService.searchBookByIsbn("9781484465479");

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals("Harry Potter and the Sorcerer's Stone", result.title());
        Assertions.assertEquals("9781484465479", result.isbn());
    }
}
