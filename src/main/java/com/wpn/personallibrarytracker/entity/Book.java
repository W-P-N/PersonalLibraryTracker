package com.wpn.personallibrarytracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Entity
@Data
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bookId;
    private String title;
    private String author;
    private Integer totalPages;
    @Column(unique = true, nullable = true)
    private String isbn;
    private String coverUrl;
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<ReadingSession> readingSessions;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<Note> notes;
    @OneToOne(mappedBy = "book", cascade = CascadeType.ALL)
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
