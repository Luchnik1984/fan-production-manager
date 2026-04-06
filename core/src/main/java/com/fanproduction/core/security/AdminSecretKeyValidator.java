package com.fanproduction.core.security;

import com.fanproduction.core.util.EnvLoader;

public class AdminSecretKeyValidator {

    private static final String SECRET_KEY = EnvLoader.get("ADMIN_SECRET_KEY");

    public static boolean validate(String providedKey) {
        if (providedKey == null || providedKey.isEmpty()) {
            return false;
        }
        return SECRET_KEY.equals(providedKey);
    }
}
