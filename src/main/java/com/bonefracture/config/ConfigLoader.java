package com.bonefracture.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    public Properties load(String path) throws ConfigurationException {
        Properties properties = new Properties();
        try (InputStream in = new FileInputStream(path)) {
            properties.load(in);
        } catch (IOException e) {
            throw new ConfigurationException("Could not read configuration file: " + path, e);
        }
        return properties;
    }
}
