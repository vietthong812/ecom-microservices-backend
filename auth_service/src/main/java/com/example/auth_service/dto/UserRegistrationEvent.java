package com.example.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//kafka: dto dùng để truyền dữ liệu tạo user cho user_service
public class UserRegistrationEvent {
    private String userId;
    private String email;
    private String fullName;
    private String phoneNumber;
}