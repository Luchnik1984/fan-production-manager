package com.fanproduction.core.launcher;

import org.springframework.context.ApplicationContext;

public interface SpringContextProvider {
    ApplicationContext getContext();
    <T> T getBean(Class<T> beanClass);
    void close();
}
