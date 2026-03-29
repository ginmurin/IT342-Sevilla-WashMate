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
        
        // Robust search for .env file
        File envFile = findEnvFile(workingDir);
        
        if (envFile == null) {
            System.out.println("⚠️ .env file not found. Skipping dotenv loading.");
            return;
        }

        System.out.println("✅ Loading .env from: " + envFile.getAbsolutePath());

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
                
                // Automatically append SSL mode for Supabase connections if missing
                if ("DB_URL".equals(key) && value.contains("supabase.com")) {
                    boolean modified = false;
                    
                    if (!value.contains("sslmode=")) {
                        if (value.contains("?")) value += "&sslmode=require";
                        else value += "?sslmode=require";
                        modified = true;
                    }
                    
                    // Transaction pooler requires disabling prepared statements
                    if (value.contains("pooler") && !value.contains("prepareThreshold=")) {
                        if (value.contains("?")) value += "&prepareThreshold=0";
                        else value += "?prepareThreshold=0";
                        modified = true;
                    }
                    
                    if (modified) {
                        System.out.println("ℹ️ Auto-fixed DB_URL for Supabase compatibility: " + value);
                    }
                }
                
                props.put(key, value);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read .env file: " + envFile.getAbsolutePath(), e);
        }

        if (!props.isEmpty()) {
            // Add with HIGHEST precedence to ensure it overrides defaults
            environment.getPropertySources()
                    .addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, props));
        }
    }

    private File findEnvFile(String startDir) {
        // 1. Check current directory
        File file = new File(startDir, ENV_FILE);
        if (file.exists()) return file;

        // 2. Check washmate subdirectory (standard structure)
        file = new File(startDir + "/washmate", ENV_FILE);
        if (file.exists()) return file;
        
        // 3. Check parent directory (if running from inside a module)
        File parent = new File(startDir).getParentFile();
        if (parent != null) {
             file = new File(parent, ENV_FILE);
             if (file.exists()) return file;
        }

        return null;
    }
}
