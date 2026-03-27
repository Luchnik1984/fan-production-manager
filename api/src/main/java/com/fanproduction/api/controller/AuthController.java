package com.fanproduction.api.controller;

import com.fanproduction.api.dto.AuthRequest;
import com.fanproduction.api.dto.AuthResponse;
import com.fanproduction.api.dto.RegisterRequest;
import com.fanproduction.api.security.JwtService;
import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.core.enums.Role;
import com.fanproduction.core.enums.UserStatus;
import com.fanproduction.core.security.AdminSecretKeyValidator;
import com.fanproduction.services.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            log.info("Authentication successful: {}", authentication.isAuthenticated());

            UserEntity user = userService.getUserByEmail(request.getEmail());
            String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

            AuthResponse response = new AuthResponse();
            response.setToken(token);
            response.setEmail(user.getEmail());
            response.setRole(user.getRole().name());

            return response;
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        log.info("Register attempt for email: {}", request.getEmail());

        // Проверка секретного ключа для ADMIN
        if ("ADMIN".equals(request.getRole())) {
            if (!AdminSecretKeyValidator.validate(request.getSecretKey())) {
                throw new BadCredentialsException("Invalid admin secret key");
            }
        }

        // Проверка, что роль существует
        Role role;
        try {
            role = Role.valueOf(request.getRole());
        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException("Invalid role: " + request.getRole());
        }

        // Создаём пользователя
        UserEntity newUser = new UserEntity();
        newUser.setEmail(request.getEmail().trim().toLowerCase());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setPhone(request.getPhone());
        newUser.setRole(role);

        // Устанавливаем статус: ADMIN сразу ACTIVE, остальные PENDING
        if (role == Role.ADMIN) {
            newUser.setStatus(UserStatus.ACTIVE);
        } else {
            newUser.setStatus(UserStatus.PENDING);
        }

        UserEntity savedUser = userService.register(newUser);

        // Генерируем токен для нового пользователя
        String token = jwtService.generateToken(savedUser.getEmail(), savedUser.getRole().name());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole().name());

        log.info("User registered: {}", savedUser.getEmail());

        return response;
    }
}