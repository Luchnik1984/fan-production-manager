package com.fanproduction.core.util;

import java.util.prefs.Preferences;

/**
 * Утилитный класс для сохранения пользовательских настроек между сеансами.
 * Использует Java Preferences API для хранения данных.
 * Хранит:
 * - Refresh token для автоматического входа
 * - Email пользователя
 * - Срок действия токена
 */
public class UserPreferences {

    private static final Preferences prefs = Preferences.userRoot().node("com/fanproduction/fanproductionmanager");

    // Ключи для хранения данных
    private static final String LAST_EMAIL_KEY = "lastEmail";
    private static final String REFRESH_TOKEN_KEY = "refreshToken";
    private static final String TOKEN_EXPIRY_KEY = "tokenExpiry";

    private UserPreferences() {
        // Приватный конструктор для утилитного класса.
        // Запрещаем создание экземпляров
    }

    /**
     * Сохраняет refresh token для автоматического входа
     *
     * @param email email пользователя
     * @param refreshToken refresh token для автоматического входа
     */
    public static void saveRefreshToken(String email, String refreshToken) {
        if (email == null || email.isEmpty()) {
            return;
        }
        if (refreshToken == null || refreshToken.isEmpty()) {
            return;
        }

        prefs.put(REFRESH_TOKEN_KEY, refreshToken);
        prefs.put(LAST_EMAIL_KEY, email);

        // Устанавливаем срок действия токена (7 дней)
        long expiryTime = System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000;
        prefs.putLong(TOKEN_EXPIRY_KEY, expiryTime);
    }

    /**
     * Проверяет, есть ли действительный refresh token
     *
     * @return true если есть валидный refresh token
     */
    public static boolean hasValidRefreshToken() {
        String token = prefs.get(REFRESH_TOKEN_KEY, "");
        long expiry = prefs.getLong(TOKEN_EXPIRY_KEY, 0);
        return !token.isEmpty() && expiry > System.currentTimeMillis();
    }

    /**
     * Получает сохранённый refresh token
     *
     * @return refresh token или пустую строку, если нет
     */
    public static String getRefreshToken() {
        return prefs.get(REFRESH_TOKEN_KEY, "");
    }

    /**
     * Получает сохранённый email пользователя
     *
     * @return email или пустую строку, если нет
     */
    public static String getSavedEmail() {
        return prefs.get(LAST_EMAIL_KEY, "");
    }

    /**
     * Очищает все данные автоматического входа
     */
    public static void clearRememberData() {
        prefs.remove(REFRESH_TOKEN_KEY);
        prefs.remove(LAST_EMAIL_KEY);
        prefs.remove(TOKEN_EXPIRY_KEY);
    }

    /**
     * Сохраняет только email (для поля "Запомнить меня" без пароля)
     * Используется для простого запоминания email без автоматического входа
     *
     * @param email email пользователя
     */
    public static void saveLastEmail(String email) {
        if (email != null && !email.isEmpty()) {
            prefs.put(LAST_EMAIL_KEY, email);
        }
    }

    /**
     * Получает последний сохранённый email (без проверки токена)
     *
     * @return email или пустую строку
     */
    public static String getLastEmail() {
        return prefs.get(LAST_EMAIL_KEY, "");
    }

    /**
     * Проверяет, есть ли сохранённый email
     *
     * @return true если есть
     */
    public static boolean hasLastEmail() {
        return !getLastEmail().isEmpty();
    }

    /**
     * Очищает только email (без очистки токена)
     */
    public static void clearLastEmail() {
        prefs.remove(LAST_EMAIL_KEY);
    }
}
