package com.my.edge.server.config;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {
    private Properties properties;
    public Configuration() {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("edge.properties")) {
            properties = new Properties();
            properties.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Initializing configuration failed. ", e);
        }
    }

    public String getTmpDir() {
        String defaultValue = "";
        try {
            File tempFile = File.createTempFile("test", "test");
            defaultValue = tempFile.getParentFile().getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return properties.getProperty("edge.tmp.dir", defaultValue);
    }
}
