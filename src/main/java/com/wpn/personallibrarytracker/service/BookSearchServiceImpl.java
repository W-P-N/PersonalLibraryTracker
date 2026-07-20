package com.wpn.personallibrarytracker.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wpn.personallibrarytracker.dto.bookDTOs.BookSearchResponseDTO;
import com.wpn.personallibrarytracker.exceptions.UnableToSearchBookException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service(value = "bookSearchService")
public class BookSearchServiceImpl implements BookSearchService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Environment environment;

    @Override
    @Transactional(readOnly = true)
    public List<BookSearchResponseDTO> searchBooksByTerm(String searchTerm) {
        String url = UriComponentsBuilder
                .fromUriString("https://www.googleapis.com/books/v1/volumes")
                .queryParam("q", searchTerm)
                .queryParam("key", environment.getProperty("BOOKS_API_KEY"))
                .toUriString();
        GoogleBooksResponse response;
        try {
            response = restTemplate.getForObject(url, GoogleBooksResponse.class);
        } catch (Exception e) {
            throw new UnableToSearchBookException(
                    environment.getProperty("Service.UNABLE_TO_SEARCH_BOOK")
            );
        }
        if (response == null || response.items() == null) {
            return new ArrayList<>();
        }
        return response.items().stream()
                .map(this::mapToResponseDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookSearchResponseDTO searchBookByIsbn(String isbn) {
        String url = UriComponentsBuilder
                .fromUriString("https://www.googleapis.com/books/v1/volumes")
                .queryParam("q", "isbn:" + isbn)
                .queryParam("key", environment.getProperty("BOOKS_API_KEY"))
                .toUriString();
        GoogleBooksResponse response;
        try {
            response = restTemplate.getForObject(url, GoogleBooksResponse.class);
        } catch (Exception e) {
            throw new UnableToSearchBookException(
                    environment.getProperty("Service.UNABLE_TO_SEARCH_BOOK")
            );
        }
        if (response != null && response.items() != null && !response.items().isEmpty()) {
            return mapToResponseDTO(response.items().get(0));
        }
        return null;
    }
    // Helper function to map googleBookItem to bookSearchResponseDTO.
    private BookSearchResponseDTO mapToResponseDTO(GoogleBookItem item) {
        if (item == null || item.volumeInfo() == null) {
            return null;
        }
        VolumeInfo info = item.volumeInfo();
        String title = info.title();
        String description = info.description();
        // Handle authors (join multiple authors with a comma)
        String author = null;
        if (info.authors() != null && !info.authors().isEmpty()) {
            author = String.join(", ", info.authors());
        }
        // Handle ISBN (prioritize ISBN_13, fallback to ISBN_10)
        String isbn = null;
        if (info.industryIdentifiers() != null) {
            isbn = info.industryIdentifiers().stream()
                    .filter(id -> "ISBN_13".equals(id.type()))
                    .map(IndustryIdentifier::identifier)
                    .findFirst()
                    .orElse(null);

            if (isbn == null) {
                isbn = info.industryIdentifiers().stream()
                        .filter(id -> "ISBN_10".equals(id.type()))
                        .map(IndustryIdentifier::identifier)
                        .findFirst()
                        .orElse(null);
            }
        }
        // Handle cover URL (prioritize thumbnail, fallback to smallThumbnail)
        String coverUrl = null;
        if (info.imageLinks() != null) {
            coverUrl = info.imageLinks().thumbnail() != null
                    ? info.imageLinks().thumbnail()
                    : info.imageLinks().smallThumbnail();

            if (coverUrl != null && coverUrl.startsWith("http://")) {
                coverUrl = coverUrl.replace("http://", "https://");
            }
        }
        Integer totalPages = info.pageCount();
        return new BookSearchResponseDTO(title, description, author, isbn, coverUrl, totalPages);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleBooksResponse(
            List<GoogleBookItem> items
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GoogleBookItem(
            VolumeInfo volumeInfo
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record VolumeInfo(
            String title,
            List<String> authors,
            String description,
            List<IndustryIdentifier> industryIdentifiers,
            Integer pageCount,
            ImageLinks imageLinks
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record IndustryIdentifier(
            String type,
            String identifier
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ImageLinks(
            String smallThumbnail,
            String thumbnail
    ) {}
}
