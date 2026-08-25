package com.example.auth_service.controller;


import com.example.api.PublicAuthenticationApi;
import com.example.api.UserAuthenticationApi;
import com.example.auth_service.service.AuthService;
import com.example.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class AuthController implements PublicAuthenticationApi, UserAuthenticationApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<RegisterResponse> register(RegisterRequest registerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequest));
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return PublicAuthenticationApi.super.getRequest();
    }

    @Override
    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @Override
    public ResponseEntity<LoginResponse> refreshToken(RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequest));
    }

    @Override
    public ResponseEntity<Void> logout(RefreshTokenRequest refreshTokenRequest) {
        authService.logout(refreshTokenRequest.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ValidateTokenResponse> validateToken(ValidateTokenRequest validateTokenRequest) {
        return ResponseEntity.ok(authService.validateToken(validateTokenRequest));
    }

    @Override
    public ResponseEntity<UserInfoResponse> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUserInfo());
    }

    @Override
    public ResponseEntity<Void> deleteUser(String id) {
        authService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}