package com.fanproduction.gui.util;

public class NumberFormatter {

    public static String formatNumber(Object value) {
        if (value == null) return "";
        if (value instanceof Number) {
            double doubleValue = ((Number) value).doubleValue();
            // Если целое число — показываем без десятичных
            if (doubleValue == Math.floor(doubleValue)) {
                return String.valueOf((long) doubleValue);
            }
            // Если дробное — показываем с одной десятой
            return String.format("%.1f", doubleValue);
        }
        return value.toString();
    }
}
