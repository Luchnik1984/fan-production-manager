package com.fanproduction.core.util;

public class CodeGenerator {

    public static String generateCodeFromFullMarking(String fullMarking) {
        if (fullMarking == null || fullMarking.isEmpty()) {
            return null;
        }
        return fullMarking
                .toLowerCase()
                .replaceAll("[^a-z0-9-]", "_")
                .replaceAll("_+", "_");
    }
}
