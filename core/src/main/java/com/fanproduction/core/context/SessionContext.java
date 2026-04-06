package com.fanproduction.core.context;

import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.core.enums.Role;

public class SessionContext {
    private static final ThreadLocal<UserEntity> currentUser = new ThreadLocal<>();

    public static void setCurrentUser(UserEntity user) {
        currentUser.set(user);
    }

    public static UserEntity getCurrentUser() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }

    public static boolean isAuthenticated() {
        return currentUser.get() != null;
    }

    public static boolean isAdmin() {
        UserEntity user = currentUser.get();
        return user != null && user.getRole() == Role.ADMIN;
    }

    public static boolean isEngineer() {
        UserEntity user = currentUser.get();
        return user != null && user.getRole() == Role.ENGINEER;
    }

    public static boolean isManager() {
        UserEntity user = currentUser.get();
        return user != null && user.getRole() == Role.MANAGER;
    }
}
