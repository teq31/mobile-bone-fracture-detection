package com.bonefracture.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigLoader {

    // Reads a .properties file. The result is cached for 5 minutes by CachingAspect.
    public Properties load(String path) throws ConfigurationException {
        if (path == null) {
            throw new ConfigurationException("configuration path is null");
        }
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(Path.of(path))) {
            properties.load(reader);
        } catch (IOException | InvalidPathException e) {
            throw new ConfigurationException("cannot read configuration file " + path, e);
        }
        return properties;
    }
}
