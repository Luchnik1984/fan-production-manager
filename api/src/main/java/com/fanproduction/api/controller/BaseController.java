package com.fanproduction.api.controller;

import com.fanproduction.core.entity.user.UserEntity;
import com.fanproduction.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Базовый контроллер для всех REST контроллеров API.
 * Предоставляет методы для получения информации о текущем пользователе.
 * <p>
 * Зачем:
 * - Устраняет дублирование кода в дочерних контроллерах
 * - Централизует логику получения текущего пользователя
 * - Упрощает добавление новых методов (например, получение сущности пользователя)
 */
public abstract class BaseController {

    @Autowired
    private UserService userService;

    /**
     * Возвращает email текущего аутентифицированного пользователя
     * или "system", если пользователь не аутентифицирован.
     * <p>
     * Зачем:
     * - Безопасное получение email для аудита и создания записей
     * - fallback на "system" гарантирует, что значение не будет null
     */
    protected String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            return "system";
        }
        return auth.getName();
    }

    /**
     * Возвращает сущность текущего пользователя.
     * Может пригодиться, когда нужны дополнительные данные пользователя
     * (например, роль или ID).
     * <p>
     * Зачем:
     * - Расширяемость — если понадобятся данные пользователя, не нужно дублировать
     * - Явное указание Optional — пользователь может отсутствовать
     */
    protected UserEntity getCurrentUserEntity() {
        String email = getCurrentUser();
        if ("system".equals(email)) {
            return null;
        }
        return userService.getUserByEmail(email);
    }

    /**
     * Проверяет, аутентифицирован ли текущий пользователь.
     */
    protected boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && auth.getPrincipal() != null;
    }
}