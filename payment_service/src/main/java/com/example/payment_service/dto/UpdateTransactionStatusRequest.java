package com.example.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//dto dùng để gửi dữ liệu cho userservice khi muốn update status của transaction
public class UpdateTransactionStatusRequest {
    private TransactionStatus status;
}
