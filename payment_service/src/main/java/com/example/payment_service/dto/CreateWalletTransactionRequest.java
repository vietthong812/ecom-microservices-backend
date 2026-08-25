package com.example.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//kafka: dto dùng để tạo giao dịch giữa orderservice và userservice
public class CreateWalletTransactionRequest {
    private String userId;
    private String orderId;
    private double amount;
    private String txnRef;
    private TransactionStatus status;
    private String paymentMethod;
    private String transactionType;
    private OffsetDateTime createdAt;
}
