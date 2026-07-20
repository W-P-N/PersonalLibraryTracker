package com.wpn.personallibrarytracker.controller;

import com.wpn.personallibrarytracker.dto.statsDTOs.StatsResponseDTO;
import com.wpn.personallibrarytracker.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/{userId}/stats")
public class StatsController {
    @Autowired
    StatsService statsService;

    @GetMapping
    public ResponseEntity<StatsResponseDTO> getStats(
            @PathVariable Integer userId
    ) {
        return ResponseEntity.ok(
                statsService.getStats(userId)
        );
    }
}
