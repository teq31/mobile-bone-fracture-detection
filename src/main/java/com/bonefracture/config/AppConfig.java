package com.bonefracture.config;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class AppConfig {

    public static final String DEFAULT_CONFIG_PATH="config/application.properties";

    private float defaultConfidenceThreshold;
    private long maxFileSizeBytes;
    private List<String> allowedFileTypes;
    private String llmApiUrl;
    private Language defaultLanguage;
    private ConfigLoader loader;

    private AppConfig(){
        this.loader=new ConfigLoader();
    }
    private static class Holder{
        private static final AppConfig INSTANCE=new AppConfig();
    }

    public static AppConfig getInstance(){
        return Holder.INSTANCE;
    }
    public void load(String path){
        Properties properties = loader.load(path);
        this.defaultConfidenceThreshold = Float.parseFloat(properties.getProperty("defaultConfidenceThreshold"));
        this.maxFileSizeBytes = Long.parseLong(properties.getProperty("maxFileSizeBytes"));
        this.allowedFileTypes = Arrays.stream(properties.getProperty("allowedFileTypes").split(","))
                .map(String::trim)
                .toList();
        this.llmApiUrl = properties.getProperty("llmApiUrl");
        this.defaultLanguage = Language.valueOf(properties.getProperty("defaultLanguage"));
    }
    public float getDefaultConfidenceThreshold(){
        return defaultConfidenceThreshold;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }
    public List<String> getAllowedFileTypes(){
        return allowedFileTypes;
    }
    public String getLlmApiUrl(){
        return llmApiUrl;
    }
    public Language getDefaultLanguage(){
        return defaultLanguage;
    }
}
