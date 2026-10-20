package com.example.shortlink.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.shortlink.cache.CacheLookup;
import com.example.shortlink.cache.ShortLinkCache;
import com.example.shortlink.common.BusinessException;
import com.example.shortlink.config.AppProperties;
import com.example.shortlink.dto.AccessStatsResponse;
import com.example.shortlink.dto.ShortLinkResponse;
import com.example.shortlink.entity.AccessLog;
import com.example.shortlink.entity.ShortLink;
import com.example.shortlink.mapper.AccessLogMapper;
import com.example.shortlink.mapper.ShortLinkMapper;
import com.example.shortlink.security.RedisRateLimiter;
import com.example.shortlink.util.Base62Codec;
import com.example.shortlink.util.LongUrlValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class ShortLinkService {

    private final ShortLinkMapper shortLinkMapper;
    private final AccessLogMapper accessLogMapper;
    private final ShortLinkCache shortLinkCache;
    private final LongUrlValidator longUrlValidator;
    private final RedisRateLimiter rateLimiter;
    private final AppProperties properties;

    public ShortLinkService(ShortLinkMapper shortLinkMapper,
                            AccessLogMapper accessLogMapper,
                            ShortLinkCache shortLinkCache,
                            LongUrlValidator longUrlValidator,
                            RedisRateLimiter rateLimiter,
                            AppProperties properties) {
        this.shortLinkMapper = shortLinkMapper;
        this.accessLogMapper = accessLogMapper;
        this.shortLinkCache = shortLinkCache;
        this.longUrlValidator = longUrlValidator;
        this.rateLimiter = rateLimiter;
        this.properties = properties;
    }

    @Transactional
    public ShortLinkResponse create(String rawLongUrl, String clientId) {
        String longUrl = longUrlValidator.validate(rawLongUrl);
        rateLimiter.check(clientId);

        // 先插入拿到自增 ID，再回填 Base62 短码；任一步失败都会回滚。
        ShortLink shortLink = new ShortLink();
        shortLink.setLongUrl(longUrl);
        shortLinkMapper.insert(shortLink);
        if (shortLink.getId() == null || shortLink.getId() <= 0) {
            throw new IllegalStateException("Failed to obtain generated id");
        }

        String shortCode = Base62Codec.encode(shortLink.getId());
        shortLink.setShortCode(shortCode);
        if (shortLinkMapper.updateById(shortLink) != 1) {
            throw new IllegalStateException("Failed to update short code");
        }

        return new ShortLinkResponse(shortCode, buildShortUrl(shortCode), longUrl);
    }

    public String resolve(String shortCode) {
        CacheLookup cacheLookup = shortLinkCache.get(shortCode);
        if (cacheLookup.hit()) {
            return cacheLookup.value().exists() ? cacheLookup.value().originalUrl() : null;
        }

        ShortLink shortLink = shortLinkMapper.selectOne(
                Wrappers.<ShortLink>lambdaQuery()
                        .eq(ShortLink::getShortCode, shortCode)
                        .select(ShortLink::getId, ShortLink::getLongUrl)
        );
        if (shortLink == null) {
            shortLinkCache.putMissing(shortCode);
            return null;
        }

        shortLinkCache.put(shortCode, shortLink.getLongUrl());
        return shortLink.getLongUrl();
    }

    public AccessStatsResponse stats(String shortCode) {
        Long linkCount = shortLinkMapper.selectCount(
                Wrappers.<ShortLink>lambdaQuery()
                        .eq(ShortLink::getShortCode, shortCode)
        );
        if (linkCount == null || linkCount == 0) {
            throw BusinessException.notFound("短码不存在");
        }

        Long totalVisits = accessLogMapper.selectCount(
                Wrappers.<AccessLog>lambdaQuery()
                        .eq(AccessLog::getShortCode, shortCode)
        );
        LocalDateTime todayStart = LocalDate.now(ZoneId.of(properties.getZoneId())).atStartOfDay();
        long todayVisits = accessLogMapper.countSince(shortCode, todayStart);

        AccessLog lastLog = accessLogMapper.selectOne(
                Wrappers.<AccessLog>lambdaQuery()
                        .eq(AccessLog::getShortCode, shortCode)
                        .orderByDesc(AccessLog::getVisitedAt)
                        .last("LIMIT 1")
        );

        return new AccessStatsResponse(
                shortCode,
                totalVisits == null ? 0 : totalVisits,
                todayVisits,
                lastLog == null ? null : lastLog.getVisitedAt()
        );
    }

    private String buildShortUrl(String shortCode) {
        String baseUrl = properties.getBaseUrl().replaceAll("/+$", "");
        return baseUrl + "/" + shortCode;
    }
}
