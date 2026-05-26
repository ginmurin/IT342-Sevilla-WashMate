package edu.cit.sevilla.washmate.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

@ExtendWith(MockitoExtension.class)
class DotenvEnvironmentPostProcessorTest {

    @Mock
    private ConfigurableEnvironment environment;

    @Mock
    private SpringApplication application;

    private DotenvEnvironmentPostProcessor processor;
    private String originalUserDir;
    private File tempDir;
    private File envFile;

    @BeforeEach
    void setUp() throws Exception {
        processor = new DotenvEnvironmentPostProcessor();
        originalUserDir = System.getProperty("user.dir");
        
        // Create a temporary directory for the test
        tempDir = Files.createTempDirectory("dotenv-test").toFile();
        System.setProperty("user.dir", tempDir.getAbsolutePath());
        
        envFile = new File(tempDir, ".env");
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setProperty("user.dir", originalUserDir);
        if (envFile.exists()) {
            envFile.delete();
        }
        tempDir.delete();
    }

    @Test
    void postProcessEnvironment_FileNotFound() {
        if (envFile.exists()) {
            envFile.delete();
        }

        processor.postProcessEnvironment(environment, application);

        verifyNoInteractions(environment);
    }

    @Test
    void postProcessEnvironment_Success() throws Exception {
        Files.write(envFile.toPath(), List.of(
            "KEY1=VALUE1",
            "# Comment",
            "  ",
            "INVALID_LINE",
            "DB_URL=jdbc:postgresql://supabase.com/postgres",
            "DB_URL2=jdbc:postgresql://supabase.com/postgres?sslmode=require",
            "DB_URL3=jdbc:postgresql://pooler.supabase.com/postgres"
        ));

        MutablePropertySources propertySources = new MutablePropertySources();
        when(environment.getPropertySources()).thenReturn(propertySources);

        processor.postProcessEnvironment(environment, application);

        assertNotNull(propertySources.get("dotenvProperties"));
    }

    @Test
    void postProcessEnvironment_EmptyFile() throws Exception {
        Files.write(envFile.toPath(), List.of());

        processor.postProcessEnvironment(environment, application);

        verifyNoInteractions(environment);
    }

    @Test
    void postProcessEnvironment_SupabaseUrl_NoSslMode_NoQueryParam() throws Exception {
        // DB_URL with supabase.com, no sslmode, no ? → appends ?sslmode=require
        Files.write(envFile.toPath(), List.of(
            "DB_URL=jdbc:postgresql://supabase.com/postgres"
        ));

        MutablePropertySources propertySources = new MutablePropertySources();
        when(environment.getPropertySources()).thenReturn(propertySources);

        processor.postProcessEnvironment(environment, application);

        assertNotNull(propertySources.get("dotenvProperties"));
    }

    @Test
    void postProcessEnvironment_SupabaseUrl_NoSslMode_WithQueryParam() throws Exception {
        // DB_URL with supabase.com, no sslmode, has ? → appends &sslmode=require
        Files.write(envFile.toPath(), List.of(
            "DB_URL=jdbc:postgresql://supabase.com/postgres?someOther=true"
        ));

        MutablePropertySources propertySources = new MutablePropertySources();
        when(environment.getPropertySources()).thenReturn(propertySources);

        processor.postProcessEnvironment(environment, application);

        assertNotNull(propertySources.get("dotenvProperties"));
    }

    @Test
    void postProcessEnvironment_SupabasePooler_NoPrepareThreshold() throws Exception {
        // DB_URL with pooler.supabase.com, no prepareThreshold → should add it
        Files.write(envFile.toPath(), List.of(
            "DB_URL=jdbc:postgresql://pooler.supabase.com/postgres"
        ));

        MutablePropertySources propertySources = new MutablePropertySources();
        when(environment.getPropertySources()).thenReturn(propertySources);

        processor.postProcessEnvironment(environment, application);

        assertNotNull(propertySources.get("dotenvProperties"));
    }

    @Test
    void postProcessEnvironment_SupabasePooler_WithExistingQueryAndNoPrepareThreshold() throws Exception {
        // DB_URL with pooler.supabase.com, has ?, no prepareThreshold → appends &prepareThreshold=0
        Files.write(envFile.toPath(), List.of(
            "DB_URL=jdbc:postgresql://pooler.supabase.com/postgres?sslmode=require"
        ));

        MutablePropertySources propertySources = new MutablePropertySources();
        when(environment.getPropertySources()).thenReturn(propertySources);

        processor.postProcessEnvironment(environment, application);

        assertNotNull(propertySources.get("dotenvProperties"));
    }

    @Test
    void postProcessEnvironment_SupabaseUrl_AlreadyHasSslModeAndPrepareThreshold() throws Exception {
        // DB_URL with supabase.com, already has sslmode and prepareThreshold → no modification
        Files.write(envFile.toPath(), List.of(
            "DB_URL=jdbc:postgresql://pooler.supabase.com/postgres?sslmode=require&prepareThreshold=0"
        ));

        MutablePropertySources propertySources = new MutablePropertySources();
        when(environment.getPropertySources()).thenReturn(propertySources);

        processor.postProcessEnvironment(environment, application);

        assertNotNull(propertySources.get("dotenvProperties"));
    }

    @Test
    void postProcessEnvironment_NonSupabaseUrl() throws Exception {
        // Non-supabase DB_URL — should not be modified
        Files.write(envFile.toPath(), List.of(
            "DB_URL=jdbc:postgresql://localhost:5432/mydb"
        ));

        MutablePropertySources propertySources = new MutablePropertySources();
        when(environment.getPropertySources()).thenReturn(propertySources);

        processor.postProcessEnvironment(environment, application);

        assertNotNull(propertySources.get("dotenvProperties"));
    }
}
