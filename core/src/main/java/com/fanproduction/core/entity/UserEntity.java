package com.fanproduction.core.entity;

import com.fanproduction.core.enums.Role;
import com.fanproduction.core.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table (name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    @Size(min = 4, max = 100, message = "Email должен содержать от 4 до 100 символов")
    private String email;

    @Column(nullable = false, length = 60)  // BCrypt хеш занимает 60 символов
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 4, max = 60, message = "Пароль должен содержать от 4 до 60 символов")
    private String password;

    @Column(name = "first_name", length = 50)
    @Size(min = 2, max = 50, message = "Имя должно содержать от 2 до 50 символов")
    private String firstName;

    @Column(name = "last_name", length = 50)
    @Size(min = 2, max = 50, message = "Фамилия должна содержать от 2 до 50 символов")
    private String lastName;

    @Column(length = 20)
    @Pattern(
            regexp = "^\\+7\\s?\\(?\\d{3}\\)?\\s?\\d{3}-?\\d{2}-?\\d{2}$",
            message = "Номер телефона должен соответствовать формату +7 XXX XXX-XX-XX"
    )
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "login_count")
    private Integer loginCount = 0;

    @Column(name = "enabled")
    private boolean enabled = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Вызывается перед сохранением в первый раз
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = UserStatus.PENDING;  // по умолчанию
        }
        if (loginCount == null) {
            loginCount = 0;
        }
    }

    /**
     * Вызывается перед обновлением
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
