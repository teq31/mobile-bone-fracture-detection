package com.bonefracture.api;

import com.bonefracture.cache.CacheRegistry;
import com.bonefracture.cache.CacheStats;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Statistics of the caching component: GET /api/cache/stats, DELETE /api/cache (empties all caches).
 */
@RestController
@RequestMapping("/api/cache")
public class CacheController {

    public record CacheStatsResponse(String name, int size, int maxEntries, long ttlSeconds,
                                     long hits, long misses, double hitRate, long evictions, long expirations) {

        static CacheStatsResponse from(CacheStats stats) {
            return new CacheStatsResponse(stats.name(), stats.size(), stats.maxEntries(), stats.ttlSeconds(),
                    stats.hits(), stats.misses(), stats.hitRate(), stats.evictions(), stats.expirations());
        }
    }

    @GetMapping("/stats")
    public List<CacheStatsResponse> stats() {
        return CacheRegistry.allStats().stream().map(CacheStatsResponse::from).toList();
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear() {
        CacheRegistry.clearAll();
    }
}
