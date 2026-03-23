package com.fanproduction.application;

import com.fanproduction.application.launcher.SpringContext;
import com.fanproduction.core.exception.GlobalExceptionHandler;
import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.core.util.EnvLoader;
import com.fanproduction.gui.LoginApplication;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {

        EnvLoader.loadToSystemProperties();

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        java.net.URL yamlUrl = cl.getResource("application.yml");
        System.out.println("application.yml URL: " + yamlUrl);
        if (yamlUrl == null) {
            System.out.println("❌ application.yml not found in classpath!");
        } else {
            System.out.println("✅ application.yml found");
        }

        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("Classpath: " + System.getProperty("java.class.path"));

        System.out.println("DB_HOST: " + System.getProperty("DB_HOST"));
        System.out.println("DB_URL from env: " + System.getenv("DB_URL"));

        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("✅ PostgreSQL JDBC Driver found in classpath");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ PostgreSQL JDBC Driver NOT found in classpath");
            e.printStackTrace();
        }

        // Регистрируем глобальный обработчик исключений
        GlobalExceptionHandler.register();


        //  Создаём Spring контекст
        SpringContextProvider springContext = SpringContext.getInstance();

        //  Передаём его в JavaFX приложение (окно входа)
        LoginApplication.setSpringContext(springContext);

        //  Запускаем JavaFX с окном входа
        Application.launch(LoginApplication.class, args);
    }
}
