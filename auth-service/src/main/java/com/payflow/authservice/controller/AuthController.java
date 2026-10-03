package com.payflow.authservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payflow.authservice.dto.LoginRequest;
import com.payflow.authservice.dto.LoginResponse;
import com.payflow.authservice.dto.LoginResult;
import com.payflow.authservice.dto.RegisterRequest;
import com.payflow.authservice.entity.User;
import com.payflow.authservice.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(user);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

    	LoginResult result = authService.login(request);

    	LoginResponse response = new LoginResponse(
    	        result.getAccessToken(),
    	        result.getEmail(),
    	        result.getRole()
    	);

        return ResponseEntity.ok(response);
    }
   
}