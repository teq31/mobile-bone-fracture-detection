package com.bonefracture.performance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// static because AspectJ creates the aspect, not us
public final class PerformanceMonitor {

    private static final double NANOS_PER_MILLI = 1_000_000.0;
    private static final Map<String, Counter> COUNTERS = new ConcurrentHashMap<>();
    private static volatile long slowThresholdMillis = 2_000;

    private PerformanceMonitor() {
    }

    public static void record(String operation, long durationNanos, boolean failed) {
        COUNTERS.computeIfAbsent(operation, name -> new Counter()).add(durationNanos, failed);
    }

    public static Optional<OperationStats> get(String operation) {
        Counter counter = COUNTERS.get(operation);
        return counter == null ? Optional.empty() : Optional.of(counter.snapshot(operation));
    }

    // slowest first
    public static List<OperationStats> all() {
        List<OperationStats> stats = new ArrayList<>();
        COUNTERS.forEach((operation, counter) -> stats.add(counter.snapshot(operation)));
        stats.sort(Comparator.comparingDouble(OperationStats::averageMillis).reversed()
                .thenComparing(OperationStats::operation));
        return stats;
    }

    public static boolean isSlow(long durationNanos) {
        return durationNanos > slowThresholdMillis * 1_000_000L;
    }

    public static long getSlowThresholdMillis() { return slowThresholdMillis; }

    public static void setSlowThresholdMillis(long millis) {
        if (millis <= 0) {
            throw new IllegalArgumentException("The threshold must be positive");
        }
        slowThresholdMillis = millis;
    }

    public static void reset() {
        COUNTERS.clear();
    }

    private static final class Counter {
        private long calls;
        private long failures;
        private long totalNanos;
        private long minNanos = Long.MAX_VALUE;
        private long maxNanos;
        private long lastNanos;

        synchronized void add(long durationNanos, boolean failed) {
            calls++;
            if (failed) {
                failures++;
            }
            totalNanos += durationNanos;
            minNanos = Math.min(minNanos, durationNanos);
            maxNanos = Math.max(maxNanos, durationNanos);
            lastNanos = durationNanos;
        }

        synchronized OperationStats snapshot(String operation) {
            return new OperationStats(operation, calls, failures,
                    totalNanos / NANOS_PER_MILLI / calls, minNanos / NANOS_PER_MILLI,
                    maxNanos / NANOS_PER_MILLI, lastNanos / NANOS_PER_MILLI);
        }
    }
}
