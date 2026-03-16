package com.fanproduction.application.test;

import com.fanproduction.application.launcher.SpringContext;
import com.fanproduction.services.TestService;

public class ManualTest {
    public static void main(String[] args) {
        try {
            System.out.println("=== Manual Test ===");
            var springContext = SpringContext.getInstance();
            var testService = springContext.getBean(TestService.class);
            testService.testDatabaseConnection();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            SpringContext.getInstance().close();
        }
    }
}

