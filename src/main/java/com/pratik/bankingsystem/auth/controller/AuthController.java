package com.pratik.bankingsystem.auth.controller;

import com.pratik.bankingsystem.auth.dto.AuthResponse;
import com.pratik.bankingsystem.auth.dto.LoginRequest;
import com.pratik.bankingsystem.auth.dto.RegisterRequest;
import com.pratik.bankingsystem.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}