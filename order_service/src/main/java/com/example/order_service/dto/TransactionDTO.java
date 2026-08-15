package com.example.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//kafka: dto dùng để tạo giao dịch giữa orderservice và userservice
public class TransactionDTO {
    private String userId;
    private double amount;
    private String txnRef;
    private TransactionStatus status;
    private String paymentMethod;
    private LocalDateTime createAt;
}
