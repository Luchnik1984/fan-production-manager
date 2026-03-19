package com.fanproduction.application.config;

import com.fanproduction.core.util.EnvLoader;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        String host = EnvLoader.get("DB_HOST");
        int port = EnvLoader.getInt("DB_PORT");
        String dbName = EnvLoader.get("DB_NAME");
        String username = EnvLoader.get("DB_USERNAME");
        String password = EnvLoader.get("DB_PASSWORD");

        String url = String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);

        System.out.println("=== Database Configuration ===");
        System.out.println("URL: " + url);
        System.out.println("Username: " + username);
        System.out.println("==============================");

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        System.out.println("=== Initializing Flyway ===");
        // Диагностика - покажем, какие файлы видит Flyway
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("application/src/main/resources/db/migration").toAbsolutePath();
            System.out.println("Migration path: " + path);
            java.nio.file.Files.list(path).forEach(p -> {
                System.out.println("Found file: " + p.getFileName());
            });
        } catch (Exception e) {
            System.out.println("Error listing files: " + e.getMessage());
        }

        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .validateMigrationNaming(false)  // отключаем строгую проверку имен
                .load();
    }

    @Bean
    @DependsOn("flyway")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        System.out.println("=== Creating EntityManagerFactory (after Flyway) ===");

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.fanproduction.core.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Properties properties = new Properties();

        String ddlAuto = EnvLoader.get("HIBERNATE_DDL");
        boolean showSql = EnvLoader.getBoolean("SHOW_SQL");
        boolean formatSql = EnvLoader.getBoolean("FORMAT_SQL");

        properties.setProperty("hibernate.hbm2ddl.auto", ddlAuto);
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.show_sql", String.valueOf(showSql));
        properties.setProperty("hibernate.format_sql", String.valueOf(formatSql));

        em.setJpaProperties(properties);
        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}
