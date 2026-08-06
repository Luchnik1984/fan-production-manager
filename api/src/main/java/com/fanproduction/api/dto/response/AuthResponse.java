package com.fanproduction.api.dto.response;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String email;
    private String role;
}
