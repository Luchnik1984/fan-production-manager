package com.fanproduction.application.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = {
        "com.fanproduction.core",
        "com.fanproduction.repositories",
        "com.fanproduction.services",
        "com.fanproduction.application"
})
@EnableJpaRepositories(basePackages = "com.fanproduction.repositories")
@PropertySource("classpath:application.properties")
public class SpringConfig {
}