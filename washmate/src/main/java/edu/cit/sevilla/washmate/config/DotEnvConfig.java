package edu.cit.sevilla.washmate.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Configuration
public class DotEnvConfig {

    @PostConstruct
    public void loadEnvironmentVariables() {
        // Try loading from the .env file (working directory should be project root)
        File envFile = new File(".env");

        // If not found in root, try in washmate subdirectory
        if (!envFile.exists()) {
            envFile = new File("washmate/.env");
        }

        if (envFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Skip comments and empty lines
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }

                    // Parse KEY=VALUE
                    int equalsIndex = line.indexOf('=');
                    if (equalsIndex > 0) {
                        String key = line.substring(0, equalsIndex).trim();
                        String value = line.substring(equalsIndex + 1).trim();

                        // Remove quotes if present
                        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                                (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }

                        // Only set if not already defined (allows override via system properties)
                        if (System.getProperty(key) == null) {
                            System.setProperty(key, value);
                        }
                    }
                }
                System.out.println("✅ .env file loaded successfully from: " + envFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("⚠️  Error reading .env file: " + e.getMessage());
            }
        } else {
            System.err.println("⚠️  .env file not found at: " + envFile.getAbsolutePath());
            System.err.println("   Application may fail to connect to database.");
        }
    }
}