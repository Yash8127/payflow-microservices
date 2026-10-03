package com.payflow.authservice.dto;

import lombok.Getter;

@Getter
public class LoginResult {

    private final String accessToken;
    private final String email;
    private final String role;

    public LoginResult(String accessToken, String email, String role) {
        this.accessToken = accessToken;
        this.email = email;
        this.role = role;
    }
}