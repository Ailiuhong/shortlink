package com.example.shortlink.service;

import com.example.shortlink.entity.AccessLog;
import com.example.shortlink.event.LinkAccessEvent;
import com.example.shortlink.mapper.AccessLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccessLogService {

    private final AccessLogMapper accessLogMapper;

    public AccessLogService(AccessLogMapper accessLogMapper) {
        this.accessLogMapper = accessLogMapper;
    }

    @Transactional
    public void record(LinkAccessEvent event) {
        AccessLog accessLog = new AccessLog();
        accessLog.setShortCode(event.shortCode());
        accessLog.setClientIp(truncate(event.clientIp(), 64));
        accessLog.setUserAgent(truncate(event.userAgent(), 512));
        accessLog.setReferer(truncate(event.referer(), 1024));
        accessLog.setVisitedAt(event.visitedAt());
        accessLogMapper.insert(accessLog);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
