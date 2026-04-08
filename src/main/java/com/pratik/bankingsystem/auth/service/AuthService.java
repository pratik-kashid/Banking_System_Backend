package com.pratik.bankingsystem.auth.service;

import com.pratik.bankingsystem.auth.dto.AuthResponse;
import com.pratik.bankingsystem.auth.dto.LoginRequest;
import com.pratik.bankingsystem.auth.dto.RegisterRequest;
import com.pratik.bankingsystem.common.enums.Role;
import com.pratik.bankingsystem.common.enums.UserStatus;
import com.pratik.bankingsystem.security.jwt.JwtService;
import com.pratik.bankingsystem.user.entity.User;
import com.pratik.bankingsystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(
                savedUser.getEmail(),
                Map.of(
                        "role", savedUser.getRole().name(),
                        "userId", savedUser.getId()
                )
        );

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        String token = jwtService.generateToken(
                user.getEmail(),
                Map.of(
                        "role", user.getRole().name(),
                        "userId", user.getId()
                )
        );

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}