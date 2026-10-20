package com.example.shortlink.event;

import com.example.shortlink.service.AccessLogService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AccessLogListener {

    private final AccessLogService accessLogService;

    public AccessLogListener(AccessLogService accessLogService) {
        this.accessLogService = accessLogService;
    }

    @Async("accessLogExecutor")
    @EventListener
    public void handle(LinkAccessEvent event) {
        accessLogService.record(event);
    }
}
