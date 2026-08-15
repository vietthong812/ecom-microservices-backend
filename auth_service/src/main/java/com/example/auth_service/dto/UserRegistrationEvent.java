package com.example.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//dto dùng để truyền dữ liệu cho kafka
public class UserRegistrationEvent {
    private String userId;
    private String email;
    private String fullName;
    private String phoneNumber;
}