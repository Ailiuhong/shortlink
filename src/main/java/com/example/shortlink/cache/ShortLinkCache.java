package com.example.shortlink.cache;

import com.example.shortlink.config.AppProperties;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ShortLinkCache {

    private static final Logger log = LoggerFactory.getLogger(ShortLinkCache.class);
    private static final String KEY_PREFIX = "shortlink:code:";
    private static final String NULL_SENTINEL = "__SHORTLINK_MISSING__";

    private final Cache<String, CachedLink> localCache;
    private final StringRedisTemplate redisTemplate;
    private final AppProperties.CacheSettings settings;

    public ShortLinkCache(Cache<String, CachedLink> shortLinkLocalCache,
                          StringRedisTemplate redisTemplate,
                          AppProperties properties) {
        this.localCache = shortLinkLocalCache;
        this.redisTemplate = redisTemplate;
        this.settings = properties.getCache();
    }

    public CacheLookup get(String shortCode) {
        CachedLink localValue = localCache.getIfPresent(shortCode);
        if (localValue != null) {
            return CacheLookup.hit(localValue);
        }

        try {
            String redisValue = redisTemplate.opsForValue().get(key(shortCode));
            if (redisValue == null) {
                return CacheLookup.miss();
            }
            CachedLink value = NULL_SENTINEL.equals(redisValue)
                    ? CachedLink.missing()
                    : CachedLink.found(redisValue);
            localCache.put(shortCode, value);
            return CacheLookup.hit(value);
        } catch (DataAccessException ex) {
            log.warn("Redis read failed, fallback to database, code={}", shortCode, ex);
            return CacheLookup.miss();
        }
    }

    public void put(String shortCode, String originalUrl) {
        CachedLink value = CachedLink.found(originalUrl);
        localCache.put(shortCode, value);
        try {
            redisTemplate.opsForValue()
                    .set(key(shortCode), originalUrl, settings.getRedisTtl());
        } catch (DataAccessException ex) {
            log.warn("Redis write failed, code={}", shortCode, ex);
        }
    }

    public void putMissing(String shortCode) {
        localCache.put(shortCode, CachedLink.missing());
        try {
            redisTemplate.opsForValue()
                    .set(key(shortCode), NULL_SENTINEL, settings.getNullRedisTtl());
        } catch (DataAccessException ex) {
            log.warn("Redis negative-cache write failed, code={}", shortCode, ex);
        }
    }

    private String key(String shortCode) {
        return KEY_PREFIX + shortCode;
    }
}
