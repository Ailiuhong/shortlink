package com.example.shortlink.config;

import com.example.shortlink.cache.CachedLink;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CacheConfig {

    @Bean
    public Cache<String, CachedLink> shortLinkLocalCache(AppProperties properties) {
        AppProperties.CacheSettings settings = properties.getCache();
        return Caffeine.newBuilder()
                .maximumSize(settings.getLocalMaxSize())
                .expireAfterWrite(settings.getLocalTtl())
                .build();
    }
}
