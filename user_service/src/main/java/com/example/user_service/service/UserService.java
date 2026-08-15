package com.example.user_service.service;

import com.example.dto.UpdateProfileRequest;
import com.example.dto.UserProfileResponse;
import com.example.user_service.mapper.UserMapper;
import com.example.user_service.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserMapper userMapper;

    public UserProfileResponse getUserProfile(String userId){
        return userProfileRepository.findById(userId)
                .map(userMapper::toUserProfileResponse)
                .orElse(null);
    }

    public UserProfileResponse updateUserProfile(String userId, UpdateProfileRequest updateProfileRequest){
        return userProfileRepository.findById(userId)
                .map(userProfile -> {
                    userProfile.setFullName(updateProfileRequest.getFullName());
                    userProfile.setPhoneNumber(updateProfileRequest.getPhoneNumber());
                    userProfileRepository.save(userProfile);
                    return userMapper.toUserProfileResponse(userProfile);
                })
                .orElse(null);
    }
}
