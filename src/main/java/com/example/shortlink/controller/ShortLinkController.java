package com.example.shortlink.controller;

import com.example.shortlink.common.BusinessException;
import com.example.shortlink.config.AppProperties;
import com.example.shortlink.dto.AccessStatsResponse;
import com.example.shortlink.dto.CreateShortLinkRequest;
import com.example.shortlink.dto.ShortLinkResponse;
import com.example.shortlink.event.LinkAccessEvent;
import com.example.shortlink.service.ShortLinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
public class ShortLinkController {

    private final ShortLinkService shortLinkService;
    private final ApplicationEventPublisher eventPublisher;
    private final AppProperties properties;

    public ShortLinkController(ShortLinkService shortLinkService,
                               ApplicationEventPublisher eventPublisher,
                               AppProperties properties) {
        this.shortLinkService = shortLinkService;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
    }

    @PostMapping("/api/links")
    public ResponseEntity<ShortLinkResponse> create(@Valid @RequestBody CreateShortLinkRequest request,
                                                    HttpServletRequest servletRequest) {
        ShortLinkResponse response = shortLinkService.create(
                request.longUrl(),
                servletRequest.getRemoteAddr()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{shortCode:[0-9A-Za-z]{1,16}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode,
                                         HttpServletRequest request) {
        String longUrl = shortLinkService.resolve(shortCode);
        if (longUrl == null) {
            throw BusinessException.notFound("短码不存在");
        }

        eventPublisher.publishEvent(new LinkAccessEvent(
                shortCode,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                request.getHeader("Referer"),
                LocalDateTime.now(ZoneId.of(properties.getZoneId()))
        ));

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(longUrl))
                .build();
    }

    @GetMapping("/api/links/{shortCode}/stats")
    public AccessStatsResponse stats(@PathVariable String shortCode) {
        return shortLinkService.stats(shortCode);
    }
}
