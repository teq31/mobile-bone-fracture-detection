package com.bonefracture.performance;

// Statistics of one monitored operation, e.g. "YoloModel.detect". Durations in milliseconds.
public record OperationStats(String operation, long calls, long failures,
                             double averageMillis, double minMillis, double maxMillis, double lastMillis) {
}
