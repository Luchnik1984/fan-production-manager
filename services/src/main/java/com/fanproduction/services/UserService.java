package com.fanproduction.services;

import com.fanproduction.core.dto.ProfileDto;
import com.fanproduction.core.dto.UserDto;
import com.fanproduction.core.entity.user.UserEntity;
import com.fanproduction.core.enums.UserStatus;

import java.util.List;

public interface UserService {

    boolean authenticate(String email, String password);

    UserEntity register(UserEntity user);

    boolean isEmailExists(String email);

    boolean autoLogin(String email);

    List<UserDto> getUsersByStatus(UserStatus status);

    List<UserDto> getAllUsers();

    void approveUser(Long userId, String adminEmail);

    void rejectUser(Long userId, String adminEmail, String reason);

    void blockUser(Long userId, String adminEmail, String reason);

    void unblockUser(Long userId, String adminEmail);

    UserStatus getUserStatus(String email);

    UserEntity getUserByEmail(String email);

    UserEntity getUserById(Long id);

    ProfileDto getCurrentUserProfile(String email);

    void updateProfile(String email, String firstName, String lastName, String phone);

    void changePassword(String email, String oldPassword, String newPassword);

    void updateLoginInfo(UserEntity user);

    UserDto getUserDtoByEmail(String email);

    UserDto getUserDtoById(Long id);

}
