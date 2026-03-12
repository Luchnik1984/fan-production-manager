package com.fanproduction.application.test;

import com.fanproduction.application.launcher.SpringContext;
import com.fanproduction.services.TestService;

public class ManualTest {

    public static void main(String[] args) {
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

