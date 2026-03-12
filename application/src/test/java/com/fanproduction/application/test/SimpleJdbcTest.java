package com.fanproduction.application.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class SimpleJdbcTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5434/fanproduction";
        String user = "fanuser";
        String password = "fanpassword";

        try {
            // Явно загружаем драйвер
            Class.forName("org.postgresql.Driver");
            System.out.println("Driver loaded successfully");

            // Пробуем подключиться
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                System.out.println("Connected to database!");

                // Простой запрос
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT 1");
                    if (rs.next()) {
                        System.out.println("Query executed: " + rs.getInt(1));
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver not found: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
