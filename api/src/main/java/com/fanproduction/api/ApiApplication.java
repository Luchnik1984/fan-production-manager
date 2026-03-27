package com.fanproduction.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.fanproduction.api",
        "com.fanproduction.core",
        "com.fanproduction.repositories",
        "com.fanproduction.services"
})
@EnableJpaRepositories(basePackages = "com.fanproduction.repositories")
@EntityScan(basePackages = "com.fanproduction.core.entity")
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ApiApplication.class);
        ConfigurableEnvironment env = app.run(args).getEnvironment();

    }
}
