package com.example.user_service.mapper;

import com.example.dto.UserProfileResponse;
import com.example.user_service.entity.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserProfileResponse toUserProfileResponse(UserProfile userProfile){
        UserProfileResponse response = new UserProfileResponse();
        response.setId(userProfile.getId());
        response.setFullName(userProfile.getFullName());
        response.setEmail(userProfile.getEmail());
        response.setPhoneNumber(userProfile.getPhoneNumber());
        return response;
    }
}
