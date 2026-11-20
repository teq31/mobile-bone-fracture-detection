package com.bonefracture.cache;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The cache itself: hits and misses, expiration (TTL), removal of the least recently used entry, statistics.
 */
class CacheStoreTest {

    /** A clock the test can move forward. */
    private static class TestClock extends Clock {
        private Instant now = Instant.parse("2026-11-20T10:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }

    private final TestClock clock = new TestClock();

    @Test
    void firstReadIsMissThenHit() {
        CacheStore cache = new CacheStore("test", Duration.ofMinutes(5), 10, clock);

        assertTrue(cache.get("a").isEmpty());
        cache.put("a", "value");
        assertEquals("value", cache.get("a").orElseThrow());

        CacheStats stats = cache.stats();
        assertEquals(1, stats.hits());
        assertEquals(1, stats.misses());
        assertEquals(0.5, stats.hitRate());
        assertEquals(1, stats.size());
    }

    @Test
    void entryExpiresAfterTimeToLive() {
        CacheStore cache = new CacheStore("test", Duration.ofMinutes(5), 10, clock);
        cache.put("a", "value");

        clock.advance(Duration.ofMinutes(4));
        assertTrue(cache.get("a").isPresent());

        clock.advance(Duration.ofMinutes(1));
        assertTrue(cache.get("a").isEmpty());
        assertEquals(1, cache.stats().expirations());
        assertEquals(0, cache.stats().size());
    }

    @Test
    void fullCacheRemovesTheLeastRecentlyUsedEntry() {
        CacheStore cache = new CacheStore("test", Duration.ofMinutes(5), 2, clock);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.get("a");          // "a" is now more recent than "b"
        cache.put("c", 3);       // cache full -> "b" is removed

        assertTrue(cache.get("a").isPresent());
        assertTrue(cache.get("b").isEmpty());
        assertTrue(cache.get("c").isPresent());
        assertEquals(1, cache.stats().evictions());
    }

    @Test
    void nullIsNotCached() {
        CacheStore cache = new CacheStore("test", Duration.ofMinutes(5), 10, clock);
        cache.put("a", null);

        assertTrue(cache.get("a").isEmpty());
        assertEquals(0, cache.stats().size());
    }

    @Test
    void clearRemovesEntriesAndResetsStatistics() {
        CacheStore cache = new CacheStore("test", Duration.ofMinutes(5), 10, clock);
        cache.put("a", 1);
        cache.get("a");
        cache.get("b");

        cache.clear();

        CacheStats stats = cache.stats();
        assertEquals(0, stats.size());
        assertEquals(0, stats.hits());
        assertEquals(0, stats.misses());
        assertEquals(0, stats.hitRate());
    }
}
