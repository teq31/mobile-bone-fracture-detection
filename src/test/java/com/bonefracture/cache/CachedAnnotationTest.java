package com.bonefracture.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @Cached on any method: the result is reused for the same arguments; exceptions are not cached.
 */
class CachedAnnotationTest {

    /** Example service, as a teammate would annotate their own method. */
    static class SlowCalculator {
        int calls;

        @Cached(name = "test-calculator", ttlSeconds = 60)
        public Integer square(int x) {
            calls++;
            return x * x;
        }

        @Cached(name = "test-calculator", ttlSeconds = 60)
        public Integer failing(int x) {
            calls++;
            throw new IllegalArgumentException("bad input " + x);
        }
    }

    @BeforeEach
    void setUp() {
        CacheRegistry.clearAll();
    }

    @Test
    void sameArgumentsRunTheMethodOnce() {
        SlowCalculator calculator = new SlowCalculator();

        assertEquals(16, calculator.square(4));
        assertEquals(16, calculator.square(4));
        assertEquals(25, calculator.square(5));

        assertEquals(2, calculator.calls);
        CacheStats stats = CacheRegistry.get("test-calculator").stats();
        assertEquals(1, stats.hits());
        assertEquals(2, stats.misses());
        assertEquals(60, stats.ttlSeconds());
    }

    @Test
    void exceptionsAreNotCached() {
        SlowCalculator calculator = new SlowCalculator();

        assertThrows(IllegalArgumentException.class, () -> calculator.failing(1));
        assertThrows(IllegalArgumentException.class, () -> calculator.failing(1));

        assertEquals(2, calculator.calls);
    }
}
