package com.bonefracture.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigLoader {

    // Reads a .properties file. The result is cached for 5 minutes by CachingAspect.
    public Properties load(String path) throws ConfigurationException {
        if (path == null) {
            throw new ConfigurationException("configuration path is null");
        }
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(toPath(path))) {
            properties.load(reader);
        } catch (IOException | InvalidPathException e) {
            throw new ConfigurationException("cannot read configuration file " + path, e);
        }
        return properties;
    }

    private Path toPath(String path) {
        try {
            return Path.of(path);
        } catch (InvalidPathException e) {
            // Accept file URL style paths such as "/C:/dir/file.properties"
            // (produced by ClassLoader.getResource().getPath() on Windows).
            if (path.length() > 2 && path.charAt(0) == '/' && path.charAt(2) == ':') {
                return Paths.get(path.substring(1));
            }
            throw e;
        }
    }
}
