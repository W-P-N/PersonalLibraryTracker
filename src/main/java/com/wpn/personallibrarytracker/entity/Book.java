package com.wpn.personallibrarytracker.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Entity
@Data
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bookId;
    private String title;
    private String author;
    private String totalPages;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name="book_id", unique = true)
    private List<ReadingSession> readingSessions;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name="book_id", unique = true)
    private List<Note> notes;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="book_id", unique = true)
    private Review review;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(bookId, book.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(bookId);
    }
}
