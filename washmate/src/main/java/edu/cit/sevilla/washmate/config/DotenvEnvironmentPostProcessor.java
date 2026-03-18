package edu.cit.sevilla.washmate.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads key=value pairs from a .env file located in the working directory
 * and adds them to Spring's environment so ${KEY} placeholders resolve correctly.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "dotenvProperties";
    private static final String ENV_FILE = ".env";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String workingDir = System.getProperty("user.dir");

        // Check working dir first, then a washmate/ subdirectory (when launched from project root)
        File envFile = new File(workingDir, ENV_FILE);
        if (!envFile.exists()) {
            envFile = new File(workingDir + "/washmate", ENV_FILE);
        }
        if (!envFile.exists()) {
            return;
        }

        Map<String, Object> props = new HashMap<>();
        try {
            for (String line : Files.readAllLines(envFile.toPath())) {
                String trimmed = line.trim();
                // skip blank lines and comments
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int idx = trimmed.indexOf('=');
                if (idx < 1) {
                    continue;
                }
                String key = trimmed.substring(0, idx).trim();
                String value = trimmed.substring(idx + 1).trim();
                props.put(key, value);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read .env file: " + envFile.getAbsolutePath(), e);
        }

        if (!props.isEmpty()) {
            environment.getPropertySources()
                    .addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, props));
        }
    }
}
