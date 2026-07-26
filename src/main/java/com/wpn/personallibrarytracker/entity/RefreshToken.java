package com.wpn.personallibrarytracker.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer refreshTokenId;
    @Column(unique = true, nullable = false)
    private String token; // UUID string
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private LocalDateTime expiryDate;
}
