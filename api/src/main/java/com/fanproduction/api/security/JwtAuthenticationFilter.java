package com.fanproduction.api.security;

import com.fanproduction.core.context.SessionContext;
import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        System.out.println("=== JwtAuthenticationFilter ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Auth header: " + authHeader);
        System.out.println("Current authentication: " + SecurityContextHolder.getContext().getAuthentication());

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No Bearer token found");
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        System.out.println("Token: " + token);

        final String email = jwtService.extractEmail(token);
        System.out.println("Extracted email: " + email);

        if (email != null) {
            // Всегда обновляем SessionContext
            UserEntity user = userService.getUserByEmail(email);
            if (user != null) {
                SessionContext.setCurrentUser(user);
            }

            System.out.println("Extracted role: " + jwtService.extractRole(token));

            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
            if ((currentAuth == null || currentAuth instanceof AnonymousAuthenticationToken) && jwtService.isTokenValid(token)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                System.out.println("Authentication set: " + SecurityContextHolder.getContext().getAuthentication());
                System.out.println("Authorities: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
            } else {
                System.out.println("Skipping authentication setup. Current auth: " + currentAuth);
                System.out.println("Token valid: " + jwtService.isTokenValid(token));
            }
        }

        filterChain.doFilter(request, response);
    }
}