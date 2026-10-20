package com.example.shortlink.dto;

import java.time.LocalDateTime;

public record AccessStatsResponse(
        String shortCode,
        long totalVisits,
        long todayVisits,
        LocalDateTime lastVisitedAt
) {
}
