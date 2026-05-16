package com.fanproduction.api.controller;

import com.fanproduction.api.dto.request.AuthRequest;
import com.fanproduction.api.dto.response.AuthResponse;
import com.fanproduction.api.dto.request.RegisterRequest;
import com.fanproduction.api.security.JwtService;
import com.fanproduction.core.entity.user.UserEntity;
import com.fanproduction.core.enums.Role;
import com.fanproduction.core.enums.UserStatus;
import com.fanproduction.core.security.AdminSecretKeyValidator;
import com.fanproduction.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        UserEntity user = userService.getUserByEmail(request.getEmail());
        if (user == null) {
            log.warn("User not found: {}", request.getEmail());
            throw new BadCredentialsException("Пользователь с таким email не найден");
        }

        // Проверяем статус ДО аутентификации
        if (user.getStatus() != UserStatus.ACTIVE) {
            log.warn("User not active: {}, status: {}", request.getEmail(), user.getStatus());
            String message = switch (user.getStatus()) {
                case PENDING ->
                        "⏳ Ваша регистрация ожидает подтверждения администратором. После подтверждения вы сможете войти в систему.";
                case REJECTED -> "❌ Ваша регистрация отклонена администратором.";
                case BLOCKED -> "🔒 Ваш аккаунт заблокирован. Обратитесь к администратору.";
                default -> "Аккаунт не активирован. Статус: " + user.getStatus();
            };
            throw new BadCredentialsException(message);
        }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            log.info("Authentication successful: {}", authentication.isAuthenticated());

            String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
            String refreshToken = jwtService.generateRefreshToken(user.getEmail());

            // Обновляем информацию о последнем входе
            user.setLastLoginAt(LocalDateTime.now());
            user.setLoginCount(user.getLoginCount() + 1);
            userService.updateLoginInfo(user);

            AuthResponse response = new AuthResponse();
            response.setToken(token);
            response.setRefreshToken(refreshToken);
            response.setEmail(user.getEmail());
            response.setRole(user.getRole().name());

            return response;

    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        if ("ADMIN".equals(request.getRole())) {
            if (!AdminSecretKeyValidator.validate(request.getSecretKey())) {
                throw new IllegalArgumentException("Неверный секретный ключ администратора");
            }
        }

        Role role = Role.valueOf(request.getRole());
        UserEntity newUser = new UserEntity();
        newUser.setEmail(request.getEmail().trim().toLowerCase());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setPhone(request.getPhone());
        newUser.setRole(role);
        newUser.setStatus(role == Role.ADMIN ? UserStatus.ACTIVE : UserStatus.PENDING);

        UserEntity savedUser = userService.register(newUser);

        String token = jwtService.generateToken(savedUser.getEmail(), savedUser.getRole().name());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole().name());

        return response;
    }

    @PostMapping("/refresh")
    public AuthResponse refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BadCredentialsException("Refresh token is required");
        }

        if (!jwtService.isTokenValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        String email = jwtService.extractEmail(refreshToken);
        UserEntity user = userService.getUserByEmail(email);

        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            throw new BadCredentialsException("User not found or not active");
        }

        String newToken = jwtService.generateToken(user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtService.generateRefreshToken(user.getEmail());

        AuthResponse response = new AuthResponse();
        response.setToken(newToken);
        response.setRefreshToken(newRefreshToken);
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());

        return response;
    }
}