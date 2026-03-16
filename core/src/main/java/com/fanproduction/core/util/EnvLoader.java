package com.fanproduction.core.util;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EnvLoader {
    private static final Logger log = LoggerFactory.getLogger(EnvLoader.class);
    private static final Dotenv dotenv;
    private static final String ACTIVE_ENV;
    private static final String PROJECT_ROOT;

    static {
        // Определяем корень проекта, поднимаясь вверх до первого .env файла
        PROJECT_ROOT = findProjectRootByEnvFile();
        log.info("📂 Project root determined as: {}", PROJECT_ROOT);

        // Определяем активное окружение
        String env = System.getProperty("app.env");
        log.debug("System.getProperty('app.env') = {}", env);

        if (env == null) {
            env = System.getenv("APP_ENV");
            log.debug("System.getenv('APP_ENV') = {}", env);
        }

        if (env == null) {
            env = System.getenv("app.env");
            log.debug("System.getenv('app.env') = {}", env);
        }

        Dotenv loadedDotenv;
        String activeEnv;

        // Если профиль указан явно
        if (env != null && !env.isEmpty()) {
            String envFile = ".env." + env;
            File fileInRoot = new File(PROJECT_ROOT, envFile);
            log.info("🔍 Looking for profile file: {}", fileInRoot.getAbsolutePath());

            if (!fileInRoot.exists()) {
                log.error("❌ Environment file not found: {}", fileInRoot.getAbsolutePath());
                log.error("Please create {} in project root: {}", envFile, PROJECT_ROOT);
                throw new ExceptionInInitializerError("Missing environment file: " + envFile);
            }

            log.info("✅ Loading environment file: {}", fileInRoot.getAbsolutePath());
            loadedDotenv = Dotenv.configure()
                    .directory(PROJECT_ROOT)
                    .filename(envFile)
                    .load();
            activeEnv = env;
            log.info("📋 Active environment: {}", activeEnv);
        } else {
            // Если профиль не указан - загружаем обычный .env
            File defaultEnvFile = new File(PROJECT_ROOT, ".env");
            log.info("🔍 Looking for default .env file: {}", defaultEnvFile.getAbsolutePath());

            if (!defaultEnvFile.exists()) {
                log.error("❌ .env file not found: {}", defaultEnvFile.getAbsolutePath());
                log.error("Please create .env file in project root: {}", PROJECT_ROOT);
                log.error("Or specify profile with -Dapp.env=profile");
                throw new ExceptionInInitializerError("Missing .env file");
            }

            log.info("✅ Loading default .env file: {}", defaultEnvFile.getAbsolutePath());
            loadedDotenv = Dotenv.configure()
                    .directory(PROJECT_ROOT)
                    .filename(".env")
                    .load();
            activeEnv = "default";
            log.info("📋 Active environment: {} (.env)", activeEnv);
        }

        dotenv = loadedDotenv;
        ACTIVE_ENV = activeEnv;
    }

    /**
     * Находит корень проекта, поднимаясь вверх по директориям,
     * пока не найдёт первый .env или .env.* файл
     */
    private static String findProjectRootByEnvFile() {
        Path currentPath = Paths.get("").toAbsolutePath();
        log.info("🔍 Starting search from current directory: {}", currentPath);

        // Проверим содержимое текущей директории
        File currentDir = currentPath.toFile();
        log.info("📁 Contents of current directory:");
        File[] files = currentDir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && f.getName().startsWith(".env")) {
                    log.info("  - Found: {}", f.getName());
                }
            }
        }

        int level = 0;
        while (currentPath != null) {
            log.info("🔍 Level {}: checking directory: {}", level, currentPath);

            File envFile = new File(currentPath.toFile(), ".env");
            log.info("  Checking: {} - exists? {}", envFile.getAbsolutePath(), envFile.exists());

            if (envFile.exists()) {
                log.info("✅ Found .env at: {}", currentPath);
                return currentPath.toString();
            }

            File envDevFile = new File(currentPath.toFile(), ".env.dev");
            log.info("  Checking: {} - exists? {}", envDevFile.getAbsolutePath(), envDevFile.exists());

            if (envDevFile.exists()) {
                log.info("✅ Found .env.dev at: {}", currentPath);
                return currentPath.toString();
            }

            File envTestFile = new File(currentPath.toFile(), ".env.test");
            log.info("  Checking: {} - exists? {}", envTestFile.getAbsolutePath(), envTestFile.exists());

            if (envTestFile.exists()) {
                log.info("✅ Found .env.test at: {}", currentPath);
                return currentPath.toString();
            }

            currentPath = currentPath.getParent();
            level++;
        }

        // Если ничего не нашли, используем текущую директорию как fallback
        String fallback = Paths.get("").toAbsolutePath().toString();
        log.warn("⚠️ Could not find any .env file after searching up {} levels, using current directory as fallback: {}", level, fallback);
        return fallback;
    }

    public static String getActiveEnv() {
        return ACTIVE_ENV;
    }

    public static String getProjectRoot() {
        return PROJECT_ROOT;
    }

    public static String get(String key) {
        String systemEnv = System.getenv(key);
        if (systemEnv != null && !systemEnv.isEmpty()) {
            return systemEnv;
        }

        String envValue = dotenv.get(key);
        if (envValue == null) {
            throw new IllegalArgumentException(
                    String.format("❌ Required environment variable '%s' is not set in %s environment",
                            key, ACTIVE_ENV.equals("default") ? ".env" : ".env." + ACTIVE_ENV)
            );
        }
        return envValue;
    }

    public static int getInt(String key) {
        String value = get(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("❌ Environment variable '%s' must be an integer, but was: %s", key, value)
            );
        }
    }

    public static boolean getBoolean(String key) {
        String value = get(key);
        return Boolean.parseBoolean(value);
    }

    public static void printAllVariables() {
        log.info("=== Environment Variables ({} environment) ===",
                ACTIVE_ENV.equals("default") ? ".env" : ".env." + ACTIVE_ENV);
        dotenv.entries().forEach(entry -> {
            if (entry.getKey().toLowerCase().contains("password")) {
                log.info("  {} = [PROTECTED]", entry.getKey());
            } else {
                log.info("  {} = {}", entry.getKey(), entry.getValue());
            }
        });
    }
}