package com.fanproduction.application.launcher;

import com.fanproduction.application.config.SpringConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;

public class SpringContext {
    private static ApplicationContext context;

    public static void init() {
        if (context == null) {
            context = new AnnotationConfigApplicationContext(SpringConfig.class);

            // Для отладки - выведем все имена бинов
            System.out.println("=== Registered beans ===");
            String[] beanNames = context.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String name : beanNames) {
                System.out.println(name);
            }
            System.out.println("=== End of beans ===");
        }
    }

    public static ApplicationContext getContext() {
        if (context == null) {
            init();
        }
        return context;
    }

    public static <T> T getBean(Class<T> beanClass) {
        return getContext().getBean(beanClass);
    }

    public static void close() {
        if (context != null) {
            ((AnnotationConfigApplicationContext) context).close();
        }
    }
}
