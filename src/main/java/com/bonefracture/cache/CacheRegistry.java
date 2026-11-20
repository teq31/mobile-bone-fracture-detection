package com.bonefracture.cache;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * All the caches of the application, by name. Static, because the AspectJ aspects
 * are not Spring beans (they are woven into the classes at compile time).
 */
public final class CacheRegistry {

    /** Analysis results, by model and image content hash. */
    public static final String ANALYSIS = "analysis";
    /** Configuration files, by path. */
    public static final String CONFIGURATION = "configuration";

    private static final Map<String, CacheStore> CACHES = new ConcurrentHashMap<>();

    static {
        CACHES.put(ANALYSIS, new CacheStore(ANALYSIS, Duration.ofHours(24), 500));
        CACHES.put(CONFIGURATION, new CacheStore(CONFIGURATION, Duration.ofMinutes(5), 20));
    }

    private CacheRegistry() {
    }

    public static CacheStore get(String name) {
        CacheStore cache = CACHES.get(name);
        if (cache == null) {
            throw new IllegalArgumentException("Unknown cache: " + name);
        }
        return cache;
    }

    /** Used by @Cached: the first call with a new name creates the cache with these settings. */
    public static CacheStore getOrCreate(String name, Duration timeToLive, int maxEntries) {
        return CACHES.computeIfAbsent(name, n -> new CacheStore(n, timeToLive, maxEntries));
    }

    public static List<CacheStats> allStats() {
        List<CacheStats> stats = new ArrayList<>();
        CACHES.values().forEach(cache -> stats.add(cache.stats()));
        stats.sort((a, b) -> a.name().compareTo(b.name()));
        return stats;
    }

    public static void clearAll() {
        CACHES.values().forEach(CacheStore::clear);
    }
}
