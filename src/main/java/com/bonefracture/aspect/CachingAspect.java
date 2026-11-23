package com.bonefracture.aspect;

import com.bonefracture.cache.CacheKeys;
import com.bonefracture.cache.CacheRegistry;
import com.bonefracture.cache.CacheStore;
import com.bonefracture.cache.Cached;
import com.bonefracture.detection.DetectionModel;
import com.bonefracture.xray.XRayImage;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.time.Duration;
import java.util.Optional;

/**
 * AOP caching component (Lab 6, Problem 2). @Around advice: look in the cache first; only on a miss
 * run the real method and save its result. None of the cached classes contains caching code.
 *
 * 1. Analysis results: every DetectionModel.detect(image), keyed by model + version + image content hash,
 *    so the same X-ray never goes through the same model twice. Comparing models (a doctor runs every
 *    model on one X-ray) is cached the same way, per model.
 * 2. Configuration: ConfigLoader.load(path), keyed by path, expires after 5 minutes.
 * 3. Any method annotated with @Cached.
 *
 * Results are not cached when the method throws, returns null, or the image file cannot be read.
 */
@Aspect
public class CachingAspect {

    @Pointcut("execution(com.bonefracture.detection.AnalysisResult com.bonefracture.detection.DetectionModel+.detect(com.bonefracture.xray.XRayImage))"
            + " && this(model) && args(image)")
    void modelDetection(DetectionModel model, XRayImage image) {
    }

    @Pointcut("execution(java.util.Properties com.bonefracture.config.ConfigLoader+.load(String)) && args(path)")
    void configurationLoad(String path) {
    }

    @Pointcut("execution(@com.bonefracture.cache.Cached * *(..)) && @annotation(cached)")
    void annotatedMethod(Cached cached) {
    }

    @Around(value = "modelDetection(model, image)", argNames = "joinPoint,model,image")
    public Object cacheDetection(ProceedingJoinPoint joinPoint, DetectionModel model, XRayImage image) throws Throwable {
        Optional<String> hash = CacheKeys.imageHash(image);
        if (hash.isEmpty()) {
            return joinPoint.proceed();
        }
        String key = model.getName() + ":" + model.getVersion() + ":" + hash.get();
        return cached(CacheRegistry.get(CacheRegistry.ANALYSIS), key, joinPoint);
    }

    @Around(value = "configurationLoad(path)", argNames = "joinPoint,path")
    public Object cacheConfiguration(ProceedingJoinPoint joinPoint, String path) throws Throwable {
        return cached(CacheRegistry.get(CacheRegistry.CONFIGURATION), String.valueOf(path), joinPoint);
    }

    @Around(value = "annotatedMethod(cached)", argNames = "joinPoint,cached")
    public Object cacheAnnotated(ProceedingJoinPoint joinPoint, Cached cached) throws Throwable {
        Optional<String> key = CacheKeys.forCall(joinPoint.getSignature().toLongString(), joinPoint.getArgs());
        if (key.isEmpty()) {
            return joinPoint.proceed();
        }
        CacheStore cache = CacheRegistry.getOrCreate(cached.name(), Duration.ofSeconds(cached.ttlSeconds()), cached.maxEntries());
        return cached(cache, key.get(), joinPoint);
    }

    private Object cached(CacheStore cache, String key, ProceedingJoinPoint joinPoint) throws Throwable {
        Optional<Object> hit = cache.get(key);
        if (hit.isPresent()) {
            return hit.get();
        }
        Object result = joinPoint.proceed();
        cache.put(key, result);
        return result;
    }
}
