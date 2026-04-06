package com.fanproduction.api.security;

import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.core.security.CurrentUserProvider;
import com.fanproduction.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Реализация CurrentUserProvider для REST API.
 * Получает информацию о текущем пользователе из SecurityContextHolder.
 */
@Component
@RequiredArgsConstructor
public class JwtCurrentUserProvider implements CurrentUserProvider {

    private final UserService userService;

    @Override
    public Optional<UserEntity> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String email = authentication.getName();
        if (email == null || email.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(userService.getUserByEmail(email));
    }
}
