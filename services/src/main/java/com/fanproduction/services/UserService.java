package com.fanproduction.services;

import com.fanproduction.core.dto.UserDto;
import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.core.enums.UserStatus;

import java.util.List;

public interface UserService {
    boolean authenticate(String email, String password);
    UserEntity register(UserEntity user);
    UserEntity getUserByEmail(String email);
    boolean isEmailExists(String email);
    boolean autoLogin(String email);

    List<UserDto> getUsersByStatus(UserStatus status);
    List<UserDto> getAllUsers();
    void approveUser(Long userId, String adminEmail);
    void rejectUser(Long userId, String adminEmail, String reason);
    UserStatus getUserStatus(String email);
}
