package com.bonefracture.service;

import com.bonefracture.config.ConfigurationException;
import com.bonefracture.config.Language;

import java.util.List;

public class ConfigurationService {

    public void loadConfiguration(String path) {
        // TODO delegate to AppConfig.getInstance().load(path)
    }

    public void loadDefault() {
        // TODO delegate to AppConfig using AppConfig.DEFAULT_CONFIG_PATH
    }

    public float getConfidenceThreshold() {
        // TODO read from the AppConfig singleton
        return 0;
    }

    public long getMaxFileSizeBytes() {
        // TODO read from the AppConfig singleton
        return 0;
    }

    public List<String> getAllowedFileTypes() {
        // TODO read from the AppConfig singleton
        return null;
    }

    public Language getDefaultLanguage() {
        // TODO read from the AppConfig singleton
        return null;
    }
}
