package com.wpn.personallibrarytracker.service;

import com.wpn.personallibrarytracker.dto.statsDTOs.StatsResponseDTO;

public interface StatsService {
    StatsResponseDTO getStats(Integer userId);
}
