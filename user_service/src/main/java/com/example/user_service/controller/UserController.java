package com.example.user_service.controller;

import com.example.api.AddressApi;
import com.example.api.ProfileApi;
import com.example.dto.AddressRequest;
import com.example.dto.AddressResponse;
import com.example.dto.UpdateProfileRequest;
import com.example.dto.UserProfileResponse;
import com.example.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController implements ProfileApi {
    @Autowired
    private UserService userService;
    
    @Override
    public ResponseEntity<UserProfileResponse> getUserProfile(String userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @Override
    public ResponseEntity<UserProfileResponse> updateUserProfile(String xUserId, UpdateProfileRequest updateProfileRequest) {
        return new ResponseEntity<>(userService.updateUserProfile(xUserId, updateProfileRequest), HttpStatus.OK);
    }


}
