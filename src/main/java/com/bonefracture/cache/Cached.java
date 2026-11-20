package com.bonefracture.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Caches the result of a method (CachingAspect). The key is built from the method and its arguments;
 * an X-ray image argument is identified by the hash of its content.
 *
 * <pre>
 * {@literal @}Cached(name = "model-comparison", ttlSeconds = 600)
 * public List&lt;AnalysisResult&gt; compare(XRayImage image) { ... }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cached {

    /** Name of the cache (shown in the statistics). */
    String name();

    long ttlSeconds() default 600;

    int maxEntries() default 100;
}
