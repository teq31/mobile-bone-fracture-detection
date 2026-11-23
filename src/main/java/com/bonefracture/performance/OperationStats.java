package com.bonefracture.performance;

// durations in ms
public record OperationStats(String operation, long calls, long failures,
                             double averageMillis, double minMillis, double maxMillis, double lastMillis) {
}
