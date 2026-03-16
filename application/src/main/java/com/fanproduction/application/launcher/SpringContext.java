package com.fanproduction.application.launcher;

import com.fanproduction.application.config.SpringConfig;
import com.fanproduction.core.launcher.SpringContextProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringContext implements SpringContextProvider {
    private static volatile SpringContext instance;
    private final ApplicationContext context;

    private SpringContext() {
        System.out.println("=== Starting Spring context ===");
        this.context = new AnnotationConfigApplicationContext(SpringConfig.class);
        System.out.println("Spring context started successfully!");
    }

    public static SpringContext getInstance() {
        if (instance == null) {
            synchronized (SpringContext.class) {
                if (instance == null) {
                    instance = new SpringContext();
                }
            }
        }
        return instance;
    }

    @Override
    public ApplicationContext getContext() {
        return context;
    }

    @Override
    public <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    @Override
    public void close() {
        if (context instanceof AnnotationConfigApplicationContext) {
            ((AnnotationConfigApplicationContext) context).close();
        }
    }
}
