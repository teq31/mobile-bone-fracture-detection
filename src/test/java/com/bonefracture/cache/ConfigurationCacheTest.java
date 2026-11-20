package com.bonefracture.cache;

import com.bonefracture.config.ConfigLoader;
import com.bonefracture.config.ConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * CachingAspect on ConfigLoader.load(path): the configuration file is read once and then served
 * from the cache (5 minutes). ConfigLoader itself contains no caching code.
 */
class ConfigurationCacheTest {

    @TempDir
    Path folder;

    private final ConfigLoader loader = new ConfigLoader();

    @BeforeEach
    void setUp() {
        CacheRegistry.clearAll();
    }

    private CacheStats stats() {
        return CacheRegistry.get(CacheRegistry.CONFIGURATION).stats();
    }

    @Test
    void configurationFileIsReadOnce() throws Exception {
        Path file = folder.resolve("application.properties");
        Files.writeString(file, "confidence.threshold=0.5\n");

        Properties first = loader.load(file.toString());
        Files.writeString(file, "confidence.threshold=0.9\n");
        Properties second = loader.load(file.toString());

        assertSame(first, second);
        assertEquals("0.5", second.getProperty("confidence.threshold"));
        assertEquals(1, stats().misses());
        assertEquals(1, stats().hits());
        assertEquals(300, stats().ttlSeconds());
    }

    @Test
    void missingFileIsNotCached() {
        String path = folder.resolve("missing.properties").toString();

        assertThrows(ConfigurationException.class, () -> loader.load(path));
        assertThrows(ConfigurationException.class, () -> loader.load(path));

        assertEquals(0, stats().size());
        assertEquals(0, stats().hits());
    }
}
