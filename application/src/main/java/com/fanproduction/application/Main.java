package com.fanproduction.application;

import com.fanproduction.application.launcher.SpringContext;
import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.gui.JavaFXSpringApplication;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        // 1. Создаём Spring контекст
        SpringContextProvider springContext = SpringContext.getInstance();

        // 2. Передаём его в JavaFX
        JavaFXSpringApplication.setSpringContextProvider(springContext);

        // 3. Запускаем JavaFX
        Application.launch(JavaFXSpringApplication.class, args);
    }
}
