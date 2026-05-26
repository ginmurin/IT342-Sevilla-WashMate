package edu.cit.sevilla.washmate.config;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DotEnvConfigTest {

    private DotEnvConfig dotEnvConfig;
    private File envFile;
    private File backupFile;
    private boolean hadOriginalFile = false;

    @BeforeEach
    void setUp() throws Exception {
        dotEnvConfig = new DotEnvConfig();
        envFile = new File(".env");
        backupFile = new File(".env.bak");
        
        // Backup existing .env
        if (envFile.exists()) {
            Files.copy(envFile.toPath(), backupFile.toPath());
            hadOriginalFile = true;
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        // Restore original .env
        if (envFile.exists()) {
            envFile.delete();
        }
        
        if (hadOriginalFile && backupFile.exists()) {
            Files.move(backupFile.toPath(), envFile.toPath());
        } else if (backupFile.exists()) {
            backupFile.delete();
        }
        
        // Clean up system properties set in tests
        System.clearProperty("KEY1");
        System.clearProperty("KEY2");
        System.clearProperty("KEY3");
        System.clearProperty("KEY_ALREADY_DEFINED");
    }

    @Test
    void loadEnvironmentVariables_Success() throws Exception {
        Files.write(envFile.toPath(), List.of(
            "KEY1=VALUE1",
            "# Comment",
            "  ",
            "INVALID_LINE",
            "KEY2=\"VALUE2\"",
            "KEY3='VALUE3'"
        ));

        dotEnvConfig.loadEnvironmentVariables();

        assertEquals("VALUE1", System.getProperty("KEY1"));
        assertEquals("VALUE2", System.getProperty("KEY2"));
        assertEquals("VALUE3", System.getProperty("KEY3"));
    }

    @Test
    void loadEnvironmentVariables_FileNotFound() {
        if (envFile.exists()) {
            envFile.delete();
        }

        dotEnvConfig.loadEnvironmentVariables();
        // Should log warning but not fail
    }

    @Test
    void loadEnvironmentVariables_AlreadyDefined() throws Exception {
        System.setProperty("KEY_ALREADY_DEFINED", "ORIGINAL_VALUE");
        
        Files.write(envFile.toPath(), List.of(
            "KEY_ALREADY_DEFINED=NEW_VALUE"
        ));

        dotEnvConfig.loadEnvironmentVariables();

        assertEquals("ORIGINAL_VALUE", System.getProperty("KEY_ALREADY_DEFINED"));
    }
}
