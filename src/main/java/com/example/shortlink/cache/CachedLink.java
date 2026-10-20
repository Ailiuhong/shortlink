package com.example.shortlink.cache;

public record CachedLink(boolean exists, String originalUrl) {

    public static CachedLink found(String originalUrl) {
        return new CachedLink(true, originalUrl);
    }

    public static CachedLink missing() {
        return new CachedLink(false, null);
    }
}
