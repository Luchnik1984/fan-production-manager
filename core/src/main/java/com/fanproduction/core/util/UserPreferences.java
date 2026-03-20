package com.fanproduction.core.util;

import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.prefs.Preferences;

public class UserPreferences {
    private static final Preferences prefs = Preferences.userRoot().node("com/fanproduction/fanproductionmanager");
    private static final String LAST_EMAIL_KEY = "lastEmail";
    private static final String REMEMBER_TOKEN_KEY = "rememberToken";
    private static final String TOKEN_EXPIRY_KEY = "tokenExpiry";

    private UserPreferences() {
        // Приватный конструктор для утилитного класса
    }

    /**
     * Сохраняет данные для автоматического входа
     */
    public static void saveRememberData(String email, String password) {
        // Генерируем токен на основе email и пароля
        String token = generateToken(email, password);

        // Устанавливаем срок действия токена (30 дней)
        long expiryTime = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000;

        prefs.put(REMEMBER_TOKEN_KEY, token);
        prefs.put(LAST_EMAIL_KEY, email);
        prefs.putLong(TOKEN_EXPIRY_KEY, expiryTime);
    }

    /**
     * Проверяет, есть ли действительный токен
     */
    public static boolean hasValidToken() {
        String token = prefs.get(REMEMBER_TOKEN_KEY, "");
        long expiry = prefs.getLong(TOKEN_EXPIRY_KEY, 0);

        return !token.isEmpty() && expiry > System.currentTimeMillis();
    }

    /**
     * Получает email по сохранённому токену
     */
    public static String getEmailFromToken() {
        return prefs.get(LAST_EMAIL_KEY, "");
    }

    /**
     * Проверяет, соответствует ли пароль сохранённому токену
     */
    public static boolean validateToken(String email, String password) {
        String storedToken = prefs.get(REMEMBER_TOKEN_KEY, "");
        String computedToken = generateToken(email, password);

        return storedToken.equals(computedToken);
    }

    /**
     * Очищает все данные автологина
     */
    public static void clearRememberData() {
        prefs.remove(REMEMBER_TOKEN_KEY);
        prefs.remove(LAST_EMAIL_KEY);
        prefs.remove(TOKEN_EXPIRY_KEY);
    }

    /**
     * Генерирует токен из email и пароля.
     * Используем BCrypt для безопасного хеширования
     */
    private static String generateToken(String email, String password) {
        String data = email + ":" + password;
        // BCrypt хеш с солью
        return BCrypt.hashpw(data, BCrypt.gensalt());
    }

    // Сохраняем только email (для обычного "запомнить меня")
    public static void saveLastEmail(String email) {
        if (email != null && !email.isEmpty()) {
            prefs.put(LAST_EMAIL_KEY, email);
        }
    }

    public static String getLastEmail() {
        return prefs.get(LAST_EMAIL_KEY, "");
    }

    public static boolean hasLastEmail() {
        return !getLastEmail().isEmpty();
    }

    public static void clearLastEmail() {
        prefs.remove(LAST_EMAIL_KEY);
    }
}
