package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewCreateRequestDTO;
import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewResponseDTO;
import com.wpn.personallibrarytracker.dto.reviewDTOs.ReviewUpdateRequestDTO;
import com.wpn.personallibrarytracker.exceptions.BookNotFoundForUserException;
import com.wpn.personallibrarytracker.exceptions.ReviewAlreadyExistsException;
import com.wpn.personallibrarytracker.exceptions.ReviewNotFoundForTheBookException;
import com.wpn.personallibrarytracker.exceptions.UserNotFoundException;
import com.wpn.personallibrarytracker.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
public class ReviewControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ReviewService reviewService;
    @Autowired
    private ObjectMapper objectMapper;

    // --- Add Review Tests ---
    @Test
    void addReview_happyPath_shouldReturn201CreatedAndReview() throws Exception {
        ReviewCreateRequestDTO request = new ReviewCreateRequestDTO("Great", 5);
        ReviewResponseDTO response = new ReviewResponseDTO("Great", 5, LocalDateTime.now());

        when(reviewService.addReview(eq(1), eq(1), any(ReviewCreateRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/users/1/books/1/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Great"))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void addReview_unHappyPath_shouldReturn404_whenUserNotFound() throws Exception {
        ReviewCreateRequestDTO request = new ReviewCreateRequestDTO("Great", 5);
        when(reviewService.addReview(eq(1), eq(1), any(ReviewCreateRequestDTO.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/users/1/books/1/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addReview_unHappyPath_shouldReturn404_whenBookNotFoundForUser() throws Exception {
        ReviewCreateRequestDTO request = new ReviewCreateRequestDTO("Great", 5);
        when(reviewService.addReview(eq(1), eq(1), any(ReviewCreateRequestDTO.class)))
                .thenThrow(new BookNotFoundForUserException("Book not found"));

        mockMvc.perform(post("/users/1/books/1/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addReview_unHappyPath_shouldReturn404_whenReviewAlreadyExists() throws Exception {
        ReviewCreateRequestDTO request = new ReviewCreateRequestDTO("Great", 5);
        when(reviewService.addReview(eq(1), eq(1), any(ReviewCreateRequestDTO.class)))
                .thenThrow(new ReviewAlreadyExistsException("Review exists"));

        mockMvc.perform(post("/users/1/books/1/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // --- Get Review Tests ---
    @Test
    void getReview_happyPath_shouldReturn200OkAndReview() throws Exception {
        ReviewResponseDTO response = new ReviewResponseDTO("Nice", 4, LocalDateTime.now());
        when(reviewService.getReview(1, 1)).thenReturn(response);

        mockMvc.perform(get("/users/1/books/1/review"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Nice"))
                .andExpect(jsonPath("$.rating").value(4));
    }

    @Test
    void getReview_unHappyPath_shouldReturn404_whenUserNotFound() throws Exception {
        when(reviewService.getReview(1, 1)).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/users/1/books/1/review"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getReview_unHappyPath_shouldReturn404_whenBookNotFoundForUser() throws Exception {
        when(reviewService.getReview(1, 1)).thenThrow(new BookNotFoundForUserException("Book not found"));

        mockMvc.perform(get("/users/1/books/1/review"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getReview_unHappyPath_shouldReturn404_whenReviewNotFoundForTheBook() throws Exception {
        when(reviewService.getReview(1, 1)).thenThrow(new ReviewNotFoundForTheBookException("Review not found"));

        mockMvc.perform(get("/users/1/books/1/review"))
                .andExpect(status().isNotFound());
    }

    // --- Update Review ---
    @Test
    void updateReview_happyPath_shouldReturn200AndUpdatedReview() throws Exception {
        ReviewUpdateRequestDTO request = new ReviewUpdateRequestDTO("Updated", 3);
        ReviewResponseDTO response = new ReviewResponseDTO("Updated", 3, LocalDateTime.now());

        when(reviewService.updateReview(eq(1), eq(1), any(ReviewUpdateRequestDTO.class))).thenReturn(response);

        mockMvc.perform(patch("/users/1/books/1/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated"))
                .andExpect(jsonPath("$.rating").value(3));
    }

    @Test
    void updateReview_unHappyPath_shouldReturn404_whenUserNotFound() throws Exception {
        ReviewUpdateRequestDTO request = new ReviewUpdateRequestDTO("Updated", 3);
        when(reviewService.updateReview(eq(1), eq(1), any(ReviewUpdateRequestDTO.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(patch("/users/1/books/1/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateReview_unHappyPath_shouldReturn404_whenBookNotFoundForUser() throws Exception {
        ReviewUpdateRequestDTO request = new ReviewUpdateRequestDTO("Updated", 3);
        when(reviewService.updateReview(eq(1), eq(1), any(ReviewUpdateRequestDTO.class)))
                .thenThrow(new BookNotFoundForUserException("Book not found"));

        mockMvc.perform(patch("/users/1/books/1/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateReview_unHappyPath_shouldReturn404_whenReviewNotFoundForTheBook() throws Exception {
        ReviewUpdateRequestDTO request = new ReviewUpdateRequestDTO("Updated", 3);
        when(reviewService.updateReview(eq(1), eq(1), any(ReviewUpdateRequestDTO.class)))
                .thenThrow(new ReviewNotFoundForTheBookException("Review not found"));

        mockMvc.perform(patch("/users/1/books/1/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // --- Delete Review Tests ---
    @Test
    void deleteReview_happyPath_shouldReturn204() throws Exception {
        doNothing().when(reviewService).deleteReview(1, 1);

        mockMvc.perform(delete("/users/1/books/1/review"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReview_unHappyPath_shouldReturn404_whenUserNotFound() throws Exception {
        doThrow(new UserNotFoundException("User not found")).when(reviewService).deleteReview(1, 1);

        mockMvc.perform(delete("/users/1/books/1/review"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReview_unHappyPath_shouldReturn404_whenBookNotFoundForUser() throws Exception {
        doThrow(new BookNotFoundForUserException("Book not found")).when(reviewService).deleteReview(1, 1);

        mockMvc.perform(delete("/users/1/books/1/review"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReview_unHappyPath_shouldReturn404_whenReviewNotFoundForTheBook() throws Exception {
        doThrow(new ReviewNotFoundForTheBookException("Review not found")).when(reviewService).deleteReview(1, 1);

        mockMvc.perform(delete("/users/1/books/1/review"))
                .andExpect(status().isNotFound());
    }
}
