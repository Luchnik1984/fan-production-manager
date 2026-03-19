package com.fanproduction.services.impl;

import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.repositories.UserRepository;
import com.fanproduction.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean authenticate(String email, String password) {
        return userRepository.findByEmail(email)
                .map(user -> password.equals(user.getPassword())) // TODO: добавить шифрование
                .orElse(false);
    }

    @Override
    @Transactional
    public UserEntity register(UserEntity user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }
        return userRepository.save(user);
    }

    @Override
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
