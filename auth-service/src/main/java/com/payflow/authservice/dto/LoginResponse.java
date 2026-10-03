package com.payflow.authservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private String email;
    private String role;

    public LoginResponse(
            String accessToken,
            String email,
            String role) {

        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.email = email;
        this.role = role;
    }
}