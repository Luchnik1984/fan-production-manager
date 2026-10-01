package com.fanproduction.api.controller;

import com.fanproduction.api.dto.response.ApiResponse;
import com.fanproduction.api.dto.request.BlockRequest;
import com.fanproduction.api.dto.request.ChangePasswordRequest;
import com.fanproduction.api.dto.request.RejectRequest;
import com.fanproduction.api.dto.request.UpdateProfileRequest;
import com.fanproduction.core.dto.UserDto;
import com.fanproduction.core.entity.user.UserEntity;
import com.fanproduction.core.enums.UserStatus;
import com.fanproduction.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления пользователями.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserService userService;


    /**
     * Вспомогательный метод.
     * Возвращает email текущего пользователя или бросает исключение.
     * Используется в методах, где аутентификация обязательна.
     */
    private String getCurrentUserOrThrow() {
        if (!isAuthenticated()) {
            throw new SecurityException("Пользователь не авторизован");
        }
        return super.getCurrentUser();  // ЯВНО вызываем метод родителя
    }

    // ========== ПУБЛИЧНЫЕ МЕТОДЫ ==========

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserDto>> getAllUsers(@RequestParam(required = false) String status) {
        List<UserDto> users;
        if (status != null && !status.isEmpty()) {
            UserStatus userStatus = UserStatus.valueOf(status);
            users = userService.getUsersByStatus(userStatus);
        } else {
            users = userService.getAllUsers();
        }
        return ApiResponse.success(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserDtoById(id);
        if (user == null) {
            return ApiResponse.error("Пользователь не найден");
        }
        return ApiResponse.success(user);
    }

    /**
     * Получить информацию о текущем пользователе.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserDto> getCurrentUserInfo() {
        String email = super.getCurrentUser();  // ЯВНО вызываем метод родителя

        if ("system".equals(email)) {
            return ApiResponse.error("Пользователь не авторизован");
        }

        UserDto user = userService.getUserDtoByEmail(email);
        if (user == null) {
            return ApiResponse.error("Пользователь не найден");
        }
        return ApiResponse.success(user);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> updateCurrentUser(@RequestBody UpdateProfileRequest request) {
        String email = getCurrentUserOrThrow();
        userService.updateProfile(email, request.getFirstName(), request.getLastName(), request.getPhone());
        return ApiResponse.success("Профиль обновлён", null);
    }

    @PostMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        String email = getCurrentUserOrThrow();
        userService.changePassword(email, request.getOldPassword(), request.getNewPassword());
        return ApiResponse.success("Пароль изменён", null);
    }

    // ========== АДМИНИСТРАТИВНЫЕ МЕТОДЫ ==========

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> approveUser(@PathVariable Long id) {
        String adminEmail = getCurrentUserOrThrow();
        userService.approveUser(id, adminEmail);
        return ApiResponse.success("Пользователь подтверждён", null);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> rejectUser(@PathVariable Long id, @RequestBody RejectRequest request) {
        String adminEmail = getCurrentUserOrThrow();
        userService.rejectUser(id, adminEmail, request.getReason());
        return ApiResponse.success("Регистрация отклонена", null);
    }

    @PostMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> blockUser(@PathVariable Long id, @RequestBody(required = false) BlockRequest request) {
        String adminEmail = getCurrentUserOrThrow();
        String reason = request != null ? request.getReason() : null;
        userService.blockUser(id, adminEmail, reason);
        return ApiResponse.success("Пользователь заблокирован", null);
    }

    @PostMapping("/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> unblockUser(@PathVariable Long id) {
        String adminEmail = getCurrentUserOrThrow();
        userService.unblockUser(id, adminEmail);
        return ApiResponse.success("Пользователь разблокирован", null);
    }

    @GetMapping("/check-status")
    @PreAuthorize("permitAll()")
    public ApiResponse<String> checkUserStatus(@RequestParam String email) {
        UserEntity user = userService.getUserByEmail(email);
        if (user == null) {
            return ApiResponse.error("Пользователь не найден");
        }
        return ApiResponse.success(user.getStatus().toString());
    }
}