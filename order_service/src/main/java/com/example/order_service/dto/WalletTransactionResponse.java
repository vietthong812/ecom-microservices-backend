package com.example.order_service.dto;

import com.example.order_service.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//api call: dto dùng để lấy dữ liệu transaction từ order service để xaxc1 thực thông tin
public class WalletTransactionResponse {
    private String transactionId;
    private double amount;
    private TransactionTypeEnum type;
    private TransactionStatus status;
    private PaymentMethod paymentMethod;
    private LocalDateTime createdAt;
}
