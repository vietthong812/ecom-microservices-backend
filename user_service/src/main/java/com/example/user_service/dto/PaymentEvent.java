package com.example.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//kafka: dto dùng để gừi thông tin thanh toán cho notificationservice thực hiện
//gửi mail xác nhận giao dịch thành công
public class PaymentEvent {
    private String userName;
    private String email;
    private String phoneNumber;
    private String transactionId;
    private double amount;
    private String time;
    private double currentBalance;
}
