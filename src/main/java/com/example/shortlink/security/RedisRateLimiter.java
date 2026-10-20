package com.example.shortlink.security;

import com.example.shortlink.common.BusinessException;
import com.example.shortlink.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimiter.class);

    private final StringRedisTemplate redisTemplate;
    private final AppProperties.RateLimitSettings settings;

    public RedisRateLimiter(StringRedisTemplate redisTemplate, AppProperties properties) {
        this.redisTemplate = redisTemplate;
        this.settings = properties.getRateLimit();
    }

    public void check(String clientId) {
        if (!settings.isEnabled()) {
            return;
        }

        long windowMillis = settings.getWindow().toMillis();
        long bucket = System.currentTimeMillis() / Math.max(windowMillis, 1);
        String key = settings.getKeyPrefix() + clientId + ":" + bucket;

        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, settings.getWindow());
            }
            if (count != null && count > settings.getMaxRequests()) {
                throw BusinessException.tooManyRequests("请求过于频繁，请稍后再试");
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            // 频控是保护能力，Redis 故障时允许核心创建链路继续运行。
            log.warn("Rate limit check failed, request is allowed", ex);
        }
    }
}
