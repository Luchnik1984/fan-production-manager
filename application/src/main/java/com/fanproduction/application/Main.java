package com.fanproduction.application;

import com.fanproduction.application.launcher.SpringContext;
import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.gui.LoginApplication;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {

        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("Classpath: " + System.getProperty("java.class.path"));

        // 1. Создаём Spring контекст
        SpringContextProvider springContext = SpringContext.getInstance();

        // 2. Передаём его в JavaFX приложение (окно входа)
        LoginApplication.setSpringContext(springContext);

        // 3. Запускаем JavaFX с окном входа
        Application.launch(LoginApplication.class, args);
    }
}
