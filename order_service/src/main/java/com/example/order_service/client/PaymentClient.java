package com.example.order_service.client;

import com.example.order_service.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "PAYMENT-SERVICE"
)
public interface PaymentClient {
    @PostMapping("/api/payments/payment/create-vnpay-url")
    ModelApiResponse createPaymentUrl(
            @RequestHeader("X-User-Id") String xUserId,
            @RequestParam("orderId") String orderId,
            @RequestParam(value = "amount", required = true) Long amount,
            @RequestParam(value = "bankCode", required = false) String bankCode,
            @RequestParam(value = "PaymentMethod", required = false) String paymentMethod,
            @RequestParam(value = "TransactionType", required = false) String transactionType
    );
}
