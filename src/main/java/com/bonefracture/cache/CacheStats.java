package com.bonefracture.cache;

/**
 * Statistics of one cache. hitRate = hits / (hits + misses), 0 when the cache was never used.
 */
public record CacheStats(String name, int size, int maxEntries, long ttlSeconds,
                         long hits, long misses, long evictions, long expirations) {

    public double hitRate() {
        long requests = hits + misses;
        return requests == 0 ? 0 : (double) hits / requests;
    }
}
