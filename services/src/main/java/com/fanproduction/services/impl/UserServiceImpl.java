package com.fanproduction.services.impl;

import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.core.exception.ValidationException;
import com.fanproduction.repositories.UserRepository;
import com.fanproduction.services.UserService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

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

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }
        return userRepository.save(user);
    }

    @Override
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean autoLogin(String email) {
        // При автовходе просто проверяем, что пользователь существует и активен
        return userRepository.findByEmail(email)
                .map(user -> {
                    // Можно добавить дополнительную логику, например,
                    // проверять статус пользователя
                    return true;
                })
                .orElse(false);
    }
}
