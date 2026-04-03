package com.fanproduction.core.security;

import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.core.enums.Role;

import java.util.Optional;

/**
 * Провайдер для получения информации о текущем аутентифицированном пользователе.
 * Абстракция, позволяющая использовать разные реализации для API и GUI.
 */
public interface CurrentUserProvider {

    /**
     * Возвращает текущего пользователя, если он аутентифицирован
     */
    Optional<UserEntity> getCurrentUser();

    /**
     * Проверяет, аутентифицирован ли пользователь
     */
    default boolean isAuthenticated() {
        return getCurrentUser().isPresent();
    }

    /**
     * Проверяет, имеет ли текущий пользователь указанную роль
     */
    default boolean hasRole(Role role) {
        return getCurrentUser()
                .map(user -> user.getRole() == role)
                .orElse(false);
    }

    /**
     * Проверяет, является ли текущий пользователь администратором
     */
    default boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    /**
     * Возвращает email текущего пользователя
     */
    default String getCurrentUserEmail() {
        return getCurrentUser()
                .map(UserEntity::getEmail)
                .orElse(null);
    }
}
