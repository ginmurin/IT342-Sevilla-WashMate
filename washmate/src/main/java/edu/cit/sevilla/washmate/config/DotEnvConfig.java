package edu.cit.sevilla.washmate.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class DotEnvConfig {

    @PostConstruct
    public void loadEnvironmentVariables() {
        // Load the .env file from the washmate directory (relative to project root)
        File envFile = new File("washmate/.env");
        if (envFile.exists()) {
            Dotenv dotenv = Dotenv.configure()
                    .directory("washmate")
                    .load();

            // Set the specific environment variables we need
            String[] envKeys = {"DB_URL", "DB_USERNAME", "DB_PASSWORD", "JWT_SECRET", "JWT_EXPIRATION_MS"};

            for (String key : envKeys) {
                String value = dotenv.get(key);
                if (value != null && System.getProperty(key) == null) {
                    System.setProperty(key, value);
                }
            }

            System.out.println("✅ .env file loaded successfully from: " + envFile.getAbsolutePath());
        } else {
            System.err.println("⚠️  .env file not found at: " + envFile.getAbsolutePath());
            System.err.println("   Application may fail to connect to database.");
        }
    }
}