package com.example.auth_service.mapper;

import com.example.auth_service.entity.User;
import com.example.dto.LoginResponse;
import com.example.dto.RegisterResponse;
import com.example.dto.UserInfoResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public RegisterResponse toRegisterResponse(User user, String message) {
        RegisterResponse registerResponse = new RegisterResponse();
        registerResponse.setUserId(user.getId());
        registerResponse.setFullName(user.getFullName());
        registerResponse.setEmail(user.getEmail());
        registerResponse.setPhoneNumber(user.getPhoneNumber());
        registerResponse.setMessage(message);
        return registerResponse;
    }

    public LoginResponse toLoginResponse(String accessToken, String refreshToken, Long expiresIn) {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.accessToken(accessToken);
        loginResponse.refreshToken(refreshToken);
        loginResponse.expiresIn(expiresIn);
        return loginResponse;
    }

    public UserInfoResponse toUserInfoResponse(User user) {
        UserInfoResponse userInfoResponse = new UserInfoResponse();
        userInfoResponse.setId(user.getId());
        userInfoResponse.setFullName(user.getFullName());
        userInfoResponse.setEmail(user.getEmail());
        userInfoResponse.setPhoneNumber(user.getPhoneNumber());
        userInfoResponse.setRole(String.valueOf(user.getRole()));
        return userInfoResponse;
    }
}
