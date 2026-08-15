package com.example.auth_service.service;

import com.example.auth_service.dto.UserRegistrationEvent;
import com.example.auth_service.entity.JwtProperties;
import com.example.auth_service.entity.RefreshToken;
import com.example.auth_service.entity.RoleType;
import com.example.auth_service.entity.User;
import com.example.auth_service.mapper.UserMapper;
import com.example.auth_service.repository.RefreshTokenRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final KafkaTemplate<String, UserRegistrationEvent> kafkaTemplate; // Inject Kafka
    private final UserMapper userMapper;

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại"); // Nên dùng Custom Exception
        }
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .role(RoleType.ROLE_USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(user);
        // 2. Bắn sự kiện sang Kafka (Topic: user-registration)
        UserRegistrationEvent event = new UserRegistrationEvent(user.getId(), user.getEmail(), user.getFullName(), user.getPhoneNumber());
        kafkaTemplate.send("user-registration", event);
        return userMapper.toRegisterResponse(user, "Đăng ký thành công");
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        if (authenticate.isAuthenticated()) {
            User user = userRepository.findUserByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            RefreshToken token = RefreshToken.builder()
                    .token(refreshToken)
                    .user(user)
                    .revoked(false)
                    .expiredAt(LocalDateTime.now().plusDays(7))
                    .build();

            refreshTokenRepository.save(token);
            return userMapper.toLoginResponse(accessToken, refreshToken, jwtProperties.getAccessTokenExpiration());
        } else {
            throw new RuntimeException("Thông tin đăng nhập không hợp lệ");
        }
    }

    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token không hợp lệ"));
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    public LoginResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .filter(token -> !token.getRevoked())
                .filter(token -> token.getExpiredAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new RuntimeException("Refresh token không hợp lệ hoặc đã hết hạn"));

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);

        return userMapper.toLoginResponse(newAccessToken, refreshToken.getToken(), jwtProperties.getAccessTokenExpiration());
    }

    public ValidateTokenResponse validateToken(ValidateTokenRequest request) {
        String token = request.getToken();
        try {
            jwtService.validateToken(token);
            return new ValidateTokenResponse()
                    .valid(true);
        } catch (Exception e) {
            return new ValidateTokenResponse()
                    .valid(false);
        }
    }

    public UserInfoResponse getCurrentUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Người dùng không được xác thực");
        }
        String email = auth.getName();
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        return userMapper.toUserInfoResponse(user);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
        refreshTokenRepository.deleteAllByUserId(id);
    }
}
