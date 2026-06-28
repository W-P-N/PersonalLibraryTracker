package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.BookSearchResponseDTO;
import com.wpn.personallibrarytracker.service.BookSearchService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicBookController.class)
public class PublicBookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookSearchService bookSearchService;

    @Test
    void searchBooks_shouldReturn200AndBookList_whenQueryIsProvided() throws Exception {
        // Arrange
        BookSearchResponseDTO bookDto = new BookSearchResponseDTO(
                "Harry Potter and the Sorcerer's Stone",
                "A young boy with a great destiny...",
                "J. K. Rowling",
                "9781484465479",
                "https://books.google.com/thumbnail.jpg",
                309
        );
        Mockito.when(bookSearchService.searchBooksByTerm("harrypotter")).thenReturn(List.of(bookDto));

        // Act & Assert
        mockMvc.perform(get("/books/search")
                .param("query", "harrypotter")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Harry Potter and the Sorcerer's Stone"))
                .andExpect(jsonPath("$[0].author").value("J. K. Rowling"))
                .andExpect(jsonPath("$[0].isbn").value("9781484465479"))
                .andExpect(jsonPath("$[0].coverUrl").value("https://books.google.com/thumbnail.jpg"))
                .andExpect(jsonPath("$[0].totalPages").value(309))
                .andExpect(jsonPath("$[0].description").value("A young boy with a great destiny..."));
    }

    @Test
    void searchByIsbn_shouldReturn200AndBook_whenIsbnFound() throws Exception {
        // Arrange
        BookSearchResponseDTO bookDto = new BookSearchResponseDTO(
                "Harry Potter and the Sorcerer's Stone",
                "A young boy...",
                "J. K. Rowling",
                "9781484465479",
                "https://books.google.com/thumbnail.jpg",
                309
        );
        Mockito.when(bookSearchService.searchBookByIsbn("9781484465479")).thenReturn(bookDto);

        // Act & Assert
        mockMvc.perform(get("/books/search/isbn/9781484465479")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Harry Potter and the Sorcerer's Stone"))
                .andExpect(jsonPath("$.author").value("J. K. Rowling"))
                .andExpect(jsonPath("$.isbn").value("9781484465479"))
                .andExpect(jsonPath("$.coverUrl").value("https://books.google.com/thumbnail.jpg"))
                .andExpect(jsonPath("$.totalPages").value(309));
    }

    @Test
    void searchByIsbn_shouldReturn200AndEmptyBody_whenIsbnNotFound() throws Exception {
        // Arrange
        Mockito.when(bookSearchService.searchBookByIsbn(anyString())).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/books/search/isbn/invalidisbn")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }
}
