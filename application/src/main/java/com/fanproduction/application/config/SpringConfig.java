package com.fanproduction.application.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.fanproduction.core"
        ,        "com.fanproduction.repositories"
        ,        "com.fanproduction.services"
        ,        "com.fanproduction.gui"
})
@EnableJpaRepositories(basePackages = "com.fanproduction.repositories")
@EntityScan(basePackages = "com.fanproduction.core.entity")
@Import(DatabaseConfig.class)

public class SpringConfig {
}