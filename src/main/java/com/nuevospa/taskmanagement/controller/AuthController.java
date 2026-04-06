package com.nuevospa.taskmanagement.controller;

import com.nuevospa.taskmanagement.api.AuthenticationApi;
import com.nuevospa.taskmanagement.dto.LoginRequest;
import com.nuevospa.taskmanagement.dto.LoginResponse;
import com.nuevospa.taskmanagement.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController implements AuthenticationApi {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public ResponseEntity<LoginResponse> obtenerToken(LoginRequest loginRequest) {
        LoginResponse response = authService.autenticar(loginRequest);
        return ResponseEntity.ok(response);
    }
}
