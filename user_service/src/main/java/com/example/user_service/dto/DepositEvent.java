package com.example.user_service.dto;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
//kafka: dto dùng để gừi thông tin nạp tiền cho notificationservice thực hiện
//gửi mail xác nhận giao dịch thành công
public class DepositEvent {
    private String userName;
    private String email;
    private String phoneNumber;
    private String transactionId;
    private double amount;
    private String time;
    private double currentBalance;
}
