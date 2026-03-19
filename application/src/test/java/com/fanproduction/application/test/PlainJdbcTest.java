package com.fanproduction.application.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PlainJdbcTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5434/fanproduction";
        String user = "fanuser";
        String password = "fanpassword";
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                System.out.println("✅ Plain JDBC connection successful");
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("❌ Plain JDBC connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
