package com.fanproduction.application.config;

import com.fanproduction.core.util.EnvLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("com.fanproduction.core.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Properties properties = new Properties();

        // Получаем настройки из переменных
        String ddlAuto = EnvLoader.get("HIBERNATE_DDL");
        boolean showSql = Boolean.parseBoolean(EnvLoader.get("SHOW_SQL"));
        boolean formatSql = Boolean.parseBoolean(EnvLoader.get("FORMAT_SQL"));

        properties.setProperty("hibernate.hbm2ddl.auto", ddlAuto);
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.setProperty("hibernate.show_sql", String.valueOf(showSql));
        properties.setProperty("hibernate.format_sql", String.valueOf(formatSql));

        em.setJpaProperties(properties);
        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }
}