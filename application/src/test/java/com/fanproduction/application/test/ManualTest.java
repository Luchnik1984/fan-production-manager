package com.fanproduction.application.test;

import com.fanproduction.application.launcher.SpringContext;
import com.fanproduction.services.TestService;

public class ManualTest {

    public static void main(String[] args) {
        // ВРЕМЕННАЯ ДИАГНОСТИКА
        System.out.println("=== MANUAL TEST DIAGNOSTICS ===");
        System.out.println("Current directory: " + System.getProperty("user.dir"));
        System.out.println("Trying to load DB_HOST directly:");
        try {
            String dbHost = com.fanproduction.core.util.EnvLoader.get("DB_HOST");
            System.out.println("DB_HOST = " + dbHost);
        } catch (Exception e) {
            System.err.println("EnvLoader error: " + e.getMessage());
        }
        System.out.println("=== END DIAGNOSTICS ===");

        try {
            // Запускаем Spring
            System.out.println("Starting Spring context...");
            SpringContext.init();
            System.out.println("Spring context started successfully!");

            // Получаем тестовый сервис
            TestService testService = SpringContext.getBean(TestService.class);

            // Тестируем подключение к БД
            testService.testDatabaseConnection();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Закрываем Spring контекст
            SpringContext.close();
        }
    }
}

