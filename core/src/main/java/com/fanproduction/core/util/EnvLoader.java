package com.fanproduction.core.util;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvLoader {
    private static final Logger log = LoggerFactory.getLogger(EnvLoader.class);
    private static final Dotenv dotenv;

    static {
        try {
            // Загружаем .env файл из корня проекта
            dotenv = Dotenv.configure()
                    .load();
            log.info("✅ Loaded .env file");
        } catch (Exception e) {
            log.error("❌ Could not load .env file! Please create .env file in project root");
            throw new ExceptionInInitializerError("Missing .env file");
        }
    }

    public static String get(String key) {
        // Сначала пробуем из переменных окружения системы
        String systemEnv = System.getenv(key);
        if (systemEnv != null && !systemEnv.isEmpty()) {
            return systemEnv;
        }

        // Затем из .env файла
        String envValue = dotenv.get(key);
        if (envValue == null) {
            throw new IllegalArgumentException("❌ Required environment variable '" + key + "' is not set in .env file");
        }
        return envValue;
    }

    public static int getInt(String key) {
        String value = get(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("❌ Environment variable '" + key + "' must be an integer, but was: " + value);
        }
    }

    public static boolean getBoolean(String key) {
        String value = get(key);
        return Boolean.parseBoolean(value);
    }
}
