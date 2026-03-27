package com.fanproduction.gui.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;
    private String secretKey;  // для ADMIN
}
