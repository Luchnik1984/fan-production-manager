package com.fanproduction.api.controller;

import com.fanproduction.api.dto.AuthRequest;
import com.fanproduction.api.dto.AuthResponse;
import com.fanproduction.api.security.JwtService;
import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.services.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

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
}