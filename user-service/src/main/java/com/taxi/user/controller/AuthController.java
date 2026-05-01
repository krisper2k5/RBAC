package com.taxi.user.controller;

import com.taxi.common.security.JwtTokenProvider;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        String token = tokenProvider.createToken(request.getUsername(), request.getRole());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @Data
    static class LoginRequest {
        private String username;
        private String password;
        private String role = "USER";
    }

    @Data
    @RequiredArgsConstructor
    static class AuthResponse {
        private final String token;
    }
}