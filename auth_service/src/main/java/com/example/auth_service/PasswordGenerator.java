package com.example.auth_service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "admin123";
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("--- KẾT QUẢ MÃ HÓA ---");
        System.out.println("Mật khẩu gốc: " + rawPassword);
        System.out.println("Mật khẩu sau khi băm (BCrypt): " + encodedPassword);
        System.out.println("-----------------------");
    }
}
