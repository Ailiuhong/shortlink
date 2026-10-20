package com.example.shortlink.cache;

public record CacheLookup(boolean hit, CachedLink value) {

    public static CacheLookup hit(CachedLink value) {
        return new CacheLookup(true, value);
    }

    public static CacheLookup miss() {
        return new CacheLookup(false, null);
    }
}
