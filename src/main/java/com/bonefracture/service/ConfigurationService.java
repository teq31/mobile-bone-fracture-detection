package com.bonefracture.service;

import com.bonefracture.config.AppConfig;
import com.bonefracture.config.Language;

import java.util.List;

public class ConfigurationService {

    public void loadConfiguration(String path) {
        AppConfig.getInstance().load(path);
    }

    public void loadDefault() {
        AppConfig.getInstance().load(AppConfig.DEFAULT_CONFIG_PATH);
    }

    public float getConfidenceThreshold() {
        return AppConfig.getInstance().getDefaultConfidenceThreshold();
    }

    public long getMaxFileSizeBytes() {
        return AppConfig.getInstance().getMaxFileSizeBytes();
    }

    public List<String> getAllowedFileTypes() {
        return AppConfig.getInstance().getAllowedFileTypes();
    }

    public Language getDefaultLanguage() {
        return AppConfig.getInstance().getDefaultLanguage();
    }
}
