package com.example.shortlink.event;

import java.time.LocalDateTime;

public record LinkAccessEvent(
        String shortCode,
        String clientIp,
        String userAgent,
        String referer,
        LocalDateTime visitedAt
) {
}
