package com.example.payment_service.dto;

import lombok.Data;

@Data
//dto dùng để gửi dữ liệu cho userservice khi muốn update wallet
public class UpdateWalletRequest {
    private double amount;
    private String txnRef;
    private TransactionType transactionType;
    private PaymentMethod paymentMethod;
}
