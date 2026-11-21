package com.bonefracture.aspect;

import com.bonefracture.performance.PerformanceMonitor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AOP - performance monitoring of the X-ray analysis (Teodora, Lab 6 Problem 1 / Lab 7).
 *
 * Measures how long every step of an analysis takes, without timing code in the classes themselves:
 * the facade (XRayAnalysisService), the adapters, the preprocessing, the detection models and the heatmaps.
 * Each call is recorded in PerformanceMonitor as "ClassName.method" (calls, failures, average, min, max);
 * a call slower than the threshold (2 s by default) is logged as a warning.
 * Exceptions are counted as failures and thrown again unchanged.
 */
@Aspect
public class PerformanceMonitoringAspect {

    private static final Logger LOG = Logger.getLogger(PerformanceMonitoringAspect.class.getName());

    @Pointcut("execution(public * com.bonefracture.analysis.XRayAnalysisService+.*(..))")
    void analysisService() {
    }

    @Pointcut("execution(com.bonefracture.xray.XRayImage com.bonefracture.xray.adapter.XRayImageSource+.toXRayImage(..))")
    void imageConversion() {
    }

    @Pointcut("execution(com.bonefracture.xray.XRayImage com.bonefracture.xray.ImagePreprocessor+.*(..))")
    void preprocessing() {
    }

    @Pointcut("execution(com.bonefracture.detection.AnalysisResult com.bonefracture.detection.DetectionModel+.detect(..))")
    void detection() {
    }

    @Pointcut("execution(com.bonefracture.detection.Heatmap com.bonefracture.detection.HeatmapGenerator+.generate(..))")
    void heatmap() {
    }

    @Around("analysisService() || imageConversion() || preprocessing() || detection() || heatmap()")
    public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
        String operation = operationName(joinPoint);
        long start = System.nanoTime();
        boolean failed = false;
        try {
            return joinPoint.proceed();
        } catch (Throwable error) {
            failed = true;
            throw error;
        } finally {
            long duration = System.nanoTime() - start;
            PerformanceMonitor.record(operation, duration, failed);
            if (PerformanceMonitor.isSlow(duration)) {
                LOG.log(Level.WARNING, "Slow step: {0} took {1} ms",
                        new Object[]{operation, duration / 1_000_000});
            } else if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "{0} took {1} ms", new Object[]{operation, duration / 1_000_000});
            }
        }
    }

    // "YoloModel.detect": the class of the object that did the work, not the interface
    private static String operationName(ProceedingJoinPoint joinPoint) {
        Object target = joinPoint.getTarget();
        String type = target != null ? target.getClass().getSimpleName()
                : joinPoint.getSignature().getDeclaringType().getSimpleName();
        return type + "." + joinPoint.getSignature().getName();
    }
}
