package com.fanproduction.api.controller;

import com.fanproduction.api.dto.ApiResponse;
import com.fanproduction.api.dto.BlockRequest;
import com.fanproduction.api.dto.ChangePasswordRequest;
import com.fanproduction.api.dto.RejectRequest;
import com.fanproduction.api.dto.UpdateProfileRequest;
import com.fanproduction.core.dto.UserDto;
import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.core.enums.UserStatus;
import com.fanproduction.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Получает email текущего аутентифицированного пользователя из SecurityContext
     */
    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new SecurityException("Пользователь не авторизован");
        }
        return auth.getName();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserDto>> getAllUsers(@RequestParam(required = false) String status) {
        List<UserDto> users;
        if (status != null && !status.isEmpty()) {
            try {
                UserStatus userStatus = UserStatus.valueOf(status);
                users = userService.getUsersByStatus(userStatus);
            } catch (IllegalArgumentException e) {
                return ApiResponse.error("Invalid status: " + status);
            }
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

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserDto> getCurrentUser() {
        String email = getCurrentUserEmail();
        UserDto user = userService.getUserDtoByEmail(email);
        if (user == null) {
            return ApiResponse.error("Пользователь не найден");
        }
        return ApiResponse.success(user);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> updateCurrentUser(@RequestBody UpdateProfileRequest request) {
        String email = getCurrentUserEmail();
        userService.updateProfile(email, request.getFirstName(), request.getLastName(), request.getPhone());
        return ApiResponse.success("Профиль обновлён", null);
    }

    @PostMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        String email = getCurrentUserEmail();
        userService.changePassword(email, request.getOldPassword(), request.getNewPassword());
        return ApiResponse.success("Пароль изменён", null);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> approveUser(@PathVariable Long id) {
        String adminEmail = getCurrentUserEmail();
        userService.approveUser(id, adminEmail);
        return ApiResponse.success("Пользователь подтверждён", null);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> rejectUser(@PathVariable Long id, @RequestBody RejectRequest request) {
        String adminEmail = getCurrentUserEmail();
        userService.rejectUser(id, adminEmail, request.getReason());
        return ApiResponse.success("Регистрация отклонена", null);
    }

    @PostMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> blockUser(@PathVariable Long id, @RequestBody(required = false) BlockRequest request) {
        String adminEmail = getCurrentUserEmail();
        String reason = request != null ? request.getReason() : null;
        userService.blockUser(id, adminEmail, reason);
        return ApiResponse.success("Пользователь заблокирован", null);
    }

    @PostMapping("/{id}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> unblockUser(@PathVariable Long id) {
        String adminEmail = getCurrentUserEmail();
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