package com.fanproduction.application.test;

import com.fanproduction.core.util.EnvLoader;

public class SimpleEnvTest {
    public static void main(String[] args) {
        System.out.println("=== Simple Env Test ===");
        try {
            String dbHost = EnvLoader.get("DB_HOST");
            System.out.println("DB_HOST = " + dbHost);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
