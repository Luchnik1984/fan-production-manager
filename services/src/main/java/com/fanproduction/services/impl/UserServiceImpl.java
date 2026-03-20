package com.fanproduction.services.impl;

import com.fanproduction.core.dto.UserDto;
import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.core.enums.UserStatus;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

//    @Override
//    public boolean authenticate(String email, String password) {
//        return userRepository.findByEmail(email)
//                .map(user -> {
//                    // Пользователь должен быть ACTIVE, чтобы войти
//                    if (user.getStatus() != UserStatus.ACTIVE) {
//                        return false;
//                    }
//                    return password.equals(user.getPassword()); // TODO: добавить шифрование
//                })
//                .orElse(false);
//    }

    @Override
    public boolean authenticate(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        return userRepository.findByEmail(normalizedEmail)
                .map(user -> password.equals(user.getPassword())) // только проверка пароля
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
        String normalizedEmail = email.trim().toLowerCase();
        // При автовходе просто проверяем, что пользователь существует и активен
        return userRepository.findByEmail(normalizedEmail)
                .map(user -> {
                    // При автовходе тоже проверяем статус
                    return user.getStatus() == UserStatus.ACTIVE;
                })
                .orElse(false);
    }

    @Override
    public List<UserDto> getUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserEntity getUserByEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        return userRepository.findByEmail(normalizedEmail).orElse(null);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approveUser(Long userId, String adminEmail) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        user.setStatus(UserStatus.ACTIVE);
        user.setApprovedBy(adminEmail);
        user.setApprovedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void rejectUser(Long userId, String adminEmail, String reason) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        user.setStatus(UserStatus.REJECTED);
        user.setApprovedBy(adminEmail);
        user.setApprovedAt(LocalDateTime.now());
        user.setRejectionReason(reason);

        userRepository.save(user);
    }

    @Override
    public UserStatus getUserStatus(String email) {
        return userRepository.findByEmail(email)
                .map(UserEntity::getStatus)
                .orElse(null);
    }

    private UserDto mapToDto(UserEntity user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getApprovedBy(),
                user.getApprovedAt(),
                user.getRejectionReason()
        );
    }
}
