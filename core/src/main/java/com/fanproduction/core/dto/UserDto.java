package com.fanproduction.core.dto;

import com.fanproduction.core.enums.Role;
import com.fanproduction.core.enums.UserStatus;

import java.time.LocalDateTime;

public record UserDto(Long id,
                      String email,
                      String firstName,
                      String lastName,
                      String phone,
                      Role role,
                      UserStatus status,
                      LocalDateTime createdAt,
                      String approvedBy,
                      LocalDateTime approvedAt,
                      String rejectionReason)
{}
