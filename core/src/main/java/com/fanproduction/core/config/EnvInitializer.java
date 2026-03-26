package com.fanproduction.core.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

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

        try {
            Dotenv dotenv = Dotenv.configure()
                    .filename(envFile)
                    .ignoreIfMissing()
                    .load();

            Map<String, Object> envMap = new HashMap<>();
            dotenv.entries().forEach(entry -> {
                envMap.put(entry.getKey(), entry.getValue());
                System.out.println("Loaded: " + entry.getKey() + "=" + entry.getValue());
            });

            environment.getPropertySources().addFirst(new MapPropertySource("dotenv", envMap));

        } catch (Exception e) {
            System.out.println("Could not load " + envFile + ", using system environment variables");
        }
    }
}
