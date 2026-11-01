package com.bonefracture.service;

import com.bonefracture.config.ConfigurationException;
import com.bonefracture.config.Language;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationServiceTest {

    private String testConfigPath() {
        return getClass().getClassLoader()
                .getResource("test-application.properties")
                .getPath();
    }

    @Test
    void loadConfiguration_throwsConfigurationException_whenFileIsMissing() {
        ConfigurationService service = new ConfigurationService();

        assertThrows(ConfigurationException.class,
                () -> service.loadConfiguration("config/does-not-exist.properties"));
    }

    @Test
    void loadConfiguration_exposesConfidenceThreshold() {
        ConfigurationService service = new ConfigurationService();
        service.loadConfiguration(testConfigPath());

        assertEquals(0.5f, service.getConfidenceThreshold(), 0.0001f);
    }

    @Test
    void loadConfiguration_exposesMaxFileSize() {
        ConfigurationService service = new ConfigurationService();
        service.loadConfiguration(testConfigPath());

        assertEquals(10485760L, service.getMaxFileSizeBytes());
    }

    @Test
    void loadConfiguration_exposesAllowedFileTypes() {
        ConfigurationService service = new ConfigurationService();
        service.loadConfiguration(testConfigPath());

        assertEquals(List.of("jpg", "png", "dicom"), service.getAllowedFileTypes());
    }

    @Test
    void loadConfiguration_exposesDefaultLanguage() {
        ConfigurationService service = new ConfigurationService();
        service.loadConfiguration(testConfigPath());

        assertEquals(Language.EN, service.getDefaultLanguage());
    }
}
