package com.pg17xbootj21.controller;

import com.pg17xbootj21.dto.ErrorResponse;
import com.pg17xbootj21.dto.LoginRequest;
import com.pg17xbootj21.dto.LoginResponse;
import com.pg17xbootj21.model.User;
import com.pg17xbootj21.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.authenticate(request.getEmail(), request.getPassword());
        
        if (user == null) {
            ErrorResponse error = new ErrorResponse(
                "Unauthorized",
                "Invalid email or password",
                HttpStatus.UNAUTHORIZED.value()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        String token = authService.createSession(request.getEmail());
        LoginResponse response = new LoginResponse(token, user.getName(), user.getEmail());
        
        return ResponseEntity.ok(response);
    }
}

