package com.fanproduction.services.impl;

import com.fanproduction.core.context.SessionContext;
import com.fanproduction.core.dto.ProfileDto;
import com.fanproduction.core.dto.UserDto;
import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.enums.UserStatus;
import com.fanproduction.core.exception.ValidationException;
import com.fanproduction.repositories.UserRepository;
import com.fanproduction.services.AuditService;
import com.fanproduction.services.UserService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private AuditService auditService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private void checkAdmin() {
        if (!SessionContext.isAdmin()) {
            throw new SecurityException("Доступ запрещён. Требуются права администратора");
        }
    }

    private void checkAuthenticated() {
        if (!SessionContext.isAuthenticated()) {
            throw new SecurityException("Пользователь не авторизован");
        }
    }

    private void checkOwner(String email) {
        checkAuthenticated();
        String currentUserEmail = SessionContext.getCurrentUser().getEmail();
        if (!currentUserEmail.equals(email) && !SessionContext.isAdmin()) {
            throw new SecurityException("Доступ запрещён. Вы можете изменять только свой профиль");
        }
    }

    // ==================== ОСНОВНЫЕ МЕТОДЫ ====================

    @Override
    public boolean authenticate(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        return userRepository.findByEmail(normalizedEmail)
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }

    @Override
    @Transactional
    public UserEntity register(UserEntity user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }

        UserEntity savedUser = userRepository.save(user);

        auditService.log(savedUser.getEmail(), AuditAction.REGISTER,
                "Регистрация пользователя с ролью: " + savedUser.getRole());

        return savedUser;
    }

    @Override
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }

    @Override
    public boolean autoLogin(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .map(user -> user.getStatus() == UserStatus.ACTIVE)
                .orElse(false);
    }

    @Override
    public List<UserDto> getUsersByStatus(UserStatus status) {
        checkAdmin();
        return userRepository.findByStatus(status).stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getAllUsers() {
        checkAdmin();
        return userRepository.findAll().stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approveUser(Long userId, String adminEmail) {
        checkAdmin();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        // Разрешаем подтверждать PENDING и REJECTED
        if (user.getStatus() != UserStatus.PENDING && user.getStatus() != UserStatus.REJECTED) {
            throw new IllegalArgumentException("Подтверждение возможно только для ожидающих или отклонённых пользователей");
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setApprovedBy(adminEmail);
        user.setApprovedAt(LocalDateTime.now());

        // Очищаем причину отклонения, если была
        if (user.getStatus() == UserStatus.REJECTED) {
            user.setRejectionReason(null);
        }

        userRepository.save(user);

        auditService.log(adminEmail, AuditAction.APPROVE_USER,
                "Подтверждена регистрация пользователя: " + user.getEmail());
    }

    @Override
    @Transactional
    public void rejectUser(Long userId, String adminEmail, String reason) {
        checkAdmin();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        user.setStatus(UserStatus.REJECTED);
        user.setApprovedBy(adminEmail);
        user.setApprovedAt(LocalDateTime.now());
        user.setRejectionReason(reason);

        userRepository.save(user);

        auditService.log(adminEmail, AuditAction.REJECT_USER,
                "Отклонена регистрация пользователя: " + user.getEmail() + ". Причина: " + reason);
    }

    @Override
    @Transactional
    public void blockUser(Long userId, String adminEmail, String reason) {
        checkAdmin();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        user.setStatus(UserStatus.BLOCKED);
        user.setApprovedBy(adminEmail);
        user.setApprovedAt(LocalDateTime.now());
        user.setRejectionReason(reason != null && !reason.isEmpty() ? reason : "Блокировка");

        userRepository.save(user);

        auditService.log(adminEmail, AuditAction.BLOCK_USER,
                "Заблокирован пользователь: " + user.getEmail() + ". Причина: " + reason);
    }

    @Override
    @Transactional
    public void unblockUser(Long userId, String adminEmail) {
        checkAdmin();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        user.setStatus(UserStatus.ACTIVE);
        user.setApprovedBy(adminEmail);
        user.setApprovedAt(LocalDateTime.now());

        userRepository.save(user);

        auditService.log(adminEmail, AuditAction.UNBLOCK_USER,
                "Разблокирован пользователь: " + user.getEmail());
    }

    @Override
    public UserStatus getUserStatus(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .map(UserEntity::getStatus)
                .orElse(null);
    }

    @Override
    public UserEntity getUserByEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        return userRepository.findByEmail(normalizedEmail).orElse(null);
    }

    @Override
    public UserEntity getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public ProfileDto getCurrentUserProfile(String email) {
        checkAuthenticated();

        String normalizedEmail = email.trim().toLowerCase();
        return userRepository.findByEmail(normalizedEmail)
                .map(this::mapToProfileDto)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    @Override
    @Transactional
    public void updateProfile(String email, String firstName, String lastName, String phone) {
        checkOwner(email);

        String normalizedEmail = email.trim().toLowerCase();
        UserEntity user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        if (firstName != null) {
            user.setFirstName(firstName.trim());
        }
        if (lastName != null) {
            user.setLastName(lastName.trim());
        }
        if (phone != null) {
            user.setPhone(phone.trim());
        }

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            throw new ValidationException(violations);
        }

        userRepository.save(user);

        auditService.log(email, AuditAction.UPDATE_PROFILE,
                "Обновлён профиль пользователя: " + email);
    }

    @Override
    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        checkOwner(email);

        String normalizedEmail = email.trim().toLowerCase();
        UserEntity user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Неверный текущий пароль");
        }

        if (newPassword == null || newPassword.length() < 4) {
            throw new IllegalArgumentException("Новый пароль должен содержать не менее 4 символов");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        auditService.log(email, AuditAction.CHANGE_PASSWORD,
                "Смена пароля пользователя: " + email);
    }

    @Override
    @Transactional
    public void updateLoginInfo(UserEntity user) {
        userRepository.save(user);

        auditService.log(user.getEmail(), AuditAction.LOGIN_SUCCESS,
                "Вход в систему (счётчик входов: " + user.getLoginCount() + ")");
    }

    @Override
    public UserDto getUserDtoByEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        return userRepository.findByEmail(normalizedEmail)
                .map(this::mapToUserDto)
                .orElse(null);
    }

    @Override
    public UserDto getUserDtoById(Long id) {
        return userRepository.findById(id)
                .map(this::mapToUserDto)
                .orElse(null);
    }

    // ==================== МАППЕРЫ ====================

    private UserDto mapToUserDto(UserEntity user) {
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

    private ProfileDto mapToProfileDto(UserEntity user) {
        return new ProfileDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                user.getLoginCount()
        );
    }
}