package com.example.shortlink.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String baseUrl = "http://localhost:8080";
    private String zoneId = "Asia/Shanghai";
    private CacheSettings cache = new CacheSettings();
    private RateLimitSettings rateLimit = new RateLimitSettings();
    private UrlSettings url = new UrlSettings();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public CacheSettings getCache() {
        return cache;
    }

    public void setCache(CacheSettings cache) {
        this.cache = cache;
    }

    public RateLimitSettings getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimitSettings rateLimit) {
        this.rateLimit = rateLimit;
    }

    public UrlSettings getUrl() {
        return url;
    }

    public void setUrl(UrlSettings url) {
        this.url = url;
    }

    public static class CacheSettings {

        private long localMaxSize = 10_000;
        private Duration localTtl = Duration.ofMinutes(10);
        private Duration redisTtl = Duration.ofHours(1);
        private Duration nullRedisTtl = Duration.ofMinutes(5);

        public long getLocalMaxSize() {
            return localMaxSize;
        }

        public void setLocalMaxSize(long localMaxSize) {
            this.localMaxSize = localMaxSize;
        }

        public Duration getLocalTtl() {
            return localTtl;
        }

        public void setLocalTtl(Duration localTtl) {
            this.localTtl = localTtl;
        }

        public Duration getRedisTtl() {
            return redisTtl;
        }

        public void setRedisTtl(Duration redisTtl) {
            this.redisTtl = redisTtl;
        }

        public Duration getNullRedisTtl() {
            return nullRedisTtl;
        }

        public void setNullRedisTtl(Duration nullRedisTtl) {
            this.nullRedisTtl = nullRedisTtl;
        }
    }

    public static class RateLimitSettings {

        private boolean enabled = true;
        private long maxRequests = 20;
        private Duration window = Duration.ofMinutes(1);
        private String keyPrefix = "shortlink:rate:create:";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getMaxRequests() {
            return maxRequests;
        }

        public void setMaxRequests(long maxRequests) {
            this.maxRequests = maxRequests;
        }

        public Duration getWindow() {
            return window;
        }

        public void setWindow(Duration window) {
            this.window = window;
        }

        public String getKeyPrefix() {
            return keyPrefix;
        }

        public void setKeyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }
    }

    public static class UrlSettings {

        private List<String> blockedHosts = new ArrayList<>();

        public List<String> getBlockedHosts() {
            return blockedHosts;
        }

        public void setBlockedHosts(List<String> blockedHosts) {
            this.blockedHosts = blockedHosts;
        }
    }
}
