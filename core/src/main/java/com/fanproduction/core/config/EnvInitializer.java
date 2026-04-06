package com.fanproduction.core.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class EnvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        String activeProfile = System.getProperty("spring.profiles.active");
        if (activeProfile == null) {
            activeProfile = System.getenv("SPRING_PROFILES_ACTIVE");
        }

        String envFile;
        if (activeProfile != null && !activeProfile.isEmpty()) {
            envFile = ".env." + activeProfile;
            System.out.println("Using profile: " + activeProfile + ", loading: " + envFile);
        } else {
            envFile = ".env";
            System.out.println("No profile specified, loading default: " + envFile);
        }

        // Ищем .env файл в корне проекта
        String rootPath = Paths.get("").toAbsolutePath().toString();
        File envFilePath = new File(rootPath, envFile);

        if (!envFilePath.exists()) {
            rootPath = Paths.get("").toAbsolutePath().getParent().toString();
            envFilePath = new File(rootPath, envFile);
        }

        System.out.println("Looking for .env at: " + envFilePath.getAbsolutePath());

        if (envFilePath.exists()) {
            System.out.println("Found .env file at: " + envFilePath.getAbsolutePath());
            try {
                Dotenv dotenv = Dotenv.configure()
                        .directory(rootPath)
                        .filename(envFile)
                        .load();

                Map<String, Object> envMap = new HashMap<>();
                dotenv.entries().forEach(entry -> {
                    envMap.put(entry.getKey(), entry.getValue());
                    System.out.println("Loaded from .env: " + entry.getKey() + "=" + entry.getValue());
                });

                environment.getPropertySources().addFirst(new MapPropertySource("dotenv", envMap));

            } catch (Exception e) {
                System.out.println("Error loading .env file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println(".env file not found at: " + envFilePath.getAbsolutePath());
        }
    }
}
