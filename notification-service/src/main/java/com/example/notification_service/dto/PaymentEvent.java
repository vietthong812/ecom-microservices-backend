package com.example.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent {
    private String userName;
    private String email;
    private String phoneNumber;
    private String transactionId;
    private double amount;
    private String time;
    private double currentBalance;
}
