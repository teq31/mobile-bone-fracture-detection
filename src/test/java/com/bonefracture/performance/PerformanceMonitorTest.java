package com.bonefracture.performance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerformanceMonitorTest {

    private static final long MS = 1_000_000L;

    @BeforeEach
    void setUp() {
        PerformanceMonitor.reset();
    }

    @Test
    void recordsCallsAndDurations() {
        PerformanceMonitor.record("YoloModel.detect", 30 * MS, false);
        PerformanceMonitor.record("YoloModel.detect", 10 * MS, false);

        OperationStats stats = PerformanceMonitor.get("YoloModel.detect").orElseThrow();
        assertEquals(2, stats.calls());
        assertEquals(0, stats.failures());
        assertEquals(20.0, stats.averageMillis(), 0.001);
        assertEquals(10.0, stats.minMillis(), 0.001);
        assertEquals(30.0, stats.maxMillis(), 0.001);
        assertEquals(10.0, stats.lastMillis(), 0.001);
    }

    @Test
    void countsFailures() {
        PerformanceMonitor.record("ImageFileAdapter.toXRayImage", 1 * MS, true);
        PerformanceMonitor.record("ImageFileAdapter.toXRayImage", 1 * MS, false);

        assertEquals(1, PerformanceMonitor.get("ImageFileAdapter.toXRayImage").orElseThrow().failures());
    }

    @Test
    void slowestOperationsComeFirst() {
        PerformanceMonitor.record("ImagePreprocessor.resize", 5 * MS, false);
        PerformanceMonitor.record("YoloModel.detect", 80 * MS, false);
        PerformanceMonitor.record("HeatmapGenerator.generate", 20 * MS, false);

        List<OperationStats> all = PerformanceMonitor.all();
        assertEquals(List.of("YoloModel.detect", "HeatmapGenerator.generate", "ImagePreprocessor.resize"),
                all.stream().map(OperationStats::operation).toList());
    }

    @Test
    void slowCallsAreFlaggedAboveTheThreshold() {
        assertTrue(PerformanceMonitor.isSlow(PerformanceMonitor.getSlowThresholdMillis() * MS + 1));
        assertFalse(PerformanceMonitor.isSlow(1));
    }

    @Test
    void resetForgetsEverything() {
        PerformanceMonitor.record("YoloModel.detect", 1 * MS, false);

        PerformanceMonitor.reset();

        assertTrue(PerformanceMonitor.all().isEmpty());
        assertTrue(PerformanceMonitor.get("YoloModel.detect").isEmpty());
    }
}
