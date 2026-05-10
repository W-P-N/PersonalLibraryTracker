package com.wpn.personallibrarytracker.repository;

import com.wpn.personallibrarytracker.entity.ReadingSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Integer> {
}
