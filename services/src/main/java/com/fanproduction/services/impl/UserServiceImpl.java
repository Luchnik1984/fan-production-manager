package com.fanproduction.services.impl;

import com.fanproduction.repositories.UserRepository;
import com.fanproduction.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean authenticate(String email, String password) {
        // TODO: реальная проверка с шифрованием пароля
        // Пока просто заглушка
        return userRepository.findByEmail(email)
                .map(user -> "admin".equals(password)) // временно
                .orElse(false);
    }
}
