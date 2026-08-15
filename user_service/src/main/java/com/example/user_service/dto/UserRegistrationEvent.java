package com.example.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//kafka: dto dùng để tạo user sau khi đang kí thành công ở authservice
public class UserRegistrationEvent {
    private String userId;
    private String email;
    private String fullName;
    private String phoneNumber;
}