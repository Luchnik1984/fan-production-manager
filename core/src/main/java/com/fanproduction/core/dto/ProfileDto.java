package com.fanproduction.core.dto;

import com.fanproduction.core.enums.Role;
import com.fanproduction.core.enums.UserStatus;
import java.time.LocalDateTime;

public record ProfileDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        Role role,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt,
        Integer loginCount
) {}
