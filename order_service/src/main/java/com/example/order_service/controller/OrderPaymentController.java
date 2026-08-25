package com.example.order_service.controller;

import com.example.api.PaymentApi;
import com.example.dto.PaymentUrlResponse;
import com.example.order_service.service.OrderPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderPaymentController implements PaymentApi {

    @Autowired
    private OrderPaymentService orderPaymentService;
    @Override
    public ResponseEntity<PaymentUrlResponse> orderPayment(String orderId, String xUserId) {
        return orderPaymentService.orderPayment(orderId, xUserId);
    }
}
