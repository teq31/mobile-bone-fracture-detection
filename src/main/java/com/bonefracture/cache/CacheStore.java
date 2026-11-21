package com.bonefracture.cache;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * One named cache: entries expire after a time to live (TTL); when the cache is full the least
 * recently used entry is removed (LRU). Keeps hit / miss statistics. Thread safe.
 */
public class CacheStore {

    private record CachedValue(Object value, Instant expiresAt) {
    }

    private final String name;
    private final Duration timeToLive;
    private final int maxEntries;
    private final Clock clock;
    private final LinkedHashMap<String, CachedValue> entries;

    private long hits;
    private long misses;
    private long evictions;
    private long expirations;

    public CacheStore(String name, Duration timeToLive, int maxEntries, Clock clock) {
        if (maxEntries < 1) {
            throw new IllegalArgumentException("maxEntries must be at least 1");
        }
        this.name = name;
        this.timeToLive = timeToLive;
        this.maxEntries = maxEntries;
        this.clock = clock;
        // access order = true: reading an entry makes it the most recently used one
        this.entries = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CachedValue> eldest) {
                if (size() > CacheStore.this.maxEntries) {
                    evictions++;
                    return true;
                }
                return false;
            }
        };
    }

    public CacheStore(String name, Duration timeToLive, int maxEntries) {
        this(name, timeToLive, maxEntries, Clock.systemUTC());
    }

    /** The cached value, or empty if it is missing or expired (counted as a miss). */
    public synchronized Optional<Object> get(String key) {
        CachedValue entry = entries.get(key);
        if (entry != null && !clock.instant().isBefore(entry.expiresAt())) {
            entries.remove(key);
            expirations++;
            entry = null;
        }
        if (entry == null) {
            misses++;
            return Optional.empty();
        }
        hits++;
        return Optional.of(entry.value());
    }

    /** Null values are not cached. */
    public synchronized void put(String key, Object value) {
        if (value != null) {
            entries.put(key, new CachedValue(value, clock.instant().plus(timeToLive)));
        }
    }

    /** Removes all entries and resets the statistics. */
    public synchronized void clear() {
        entries.clear();
        hits = 0;
        misses = 0;
        evictions = 0;
        expirations = 0;
    }

    public synchronized CacheStats stats() {
        return new CacheStats(name, entries.size(), maxEntries, timeToLive.toSeconds(),
                hits, misses, evictions, expirations);
    }

    public String getName() {
        return name;
    }
}
