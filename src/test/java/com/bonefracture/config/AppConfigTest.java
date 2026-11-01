package com.bonefracture.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD - Iteration 2 (Singleton pattern).
 * Tests written BEFORE implementing AppConfig.load() and ConfigLoader.load().
 */
class AppConfigTest {

    private String testConfigPath() {
        // Resolve the test properties file from the test classpath.
        return getClass().getClassLoader()
                .getResource("test-application.properties")
                .getPath();
    }

    // Already passes because AppConfig uses the Holder idiom (Singleton is already
    // implemented), so it is not part of the RED phase. Re-enable after the GREEN phase
    // to document the Singleton invariant.
    // @Test
    // void getInstance_returnsTheSameInstanceEveryTime() {
    //     AppConfig first = AppConfig.getInstance();
    //     AppConfig second = AppConfig.getInstance();
    //
    //     assertSame(first, second);
    // }

    @Test
    void load_throwsConfigurationException_whenFileIsMissing() {
        AppConfig config = AppConfig.getInstance();

        assertThrows(ConfigurationException.class,
                () -> config.load("config/does-not-exist.properties"));
    }

    @Test
    void load_readsConfidenceThresholdFromFile() {
        AppConfig config = AppConfig.getInstance();
        config.load(testConfigPath());

        assertEquals(0.5f, config.getDefaultConfidenceThreshold(), 0.0001f);
    }

    @Test
    void load_readsMaxFileSizeFromFile() {
        AppConfig config = AppConfig.getInstance();
        config.load(testConfigPath());

        assertEquals(10485760L, config.getMaxFileSizeBytes());
    }

    @Test
    void load_readsAllowedFileTypesAsList() {
        AppConfig config = AppConfig.getInstance();
        config.load(testConfigPath());

        assertEquals(List.of("jpg", "png", "dicom"), config.getAllowedFileTypes());
    }

    @Test
    void load_readsLlmApiUrlFromFile() {
        AppConfig config = AppConfig.getInstance();
        config.load(testConfigPath());

        assertEquals("https://example.com/llm", config.getLlmApiUrl());
    }

    @Test
    void load_readsDefaultLanguageFromFile() {
        AppConfig config = AppConfig.getInstance();
        config.load(testConfigPath());

        assertEquals(Language.EN, config.getDefaultLanguage());
    }
}
