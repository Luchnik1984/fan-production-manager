package com.fanproduction.services;

import com.fanproduction.core.entity.UserEntity;

public interface UserService {
    boolean authenticate(String email, String password);
    UserEntity register(UserEntity user);
    boolean isEmailExists(String email);
}
