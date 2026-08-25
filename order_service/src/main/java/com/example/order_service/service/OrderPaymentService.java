package com.example.order_service.service;

import com.example.dto.PaymentUrlResponse;
import com.example.order_service.client.PaymentClient;
import com.example.order_service.dto.ModelApiResponse;
import com.example.order_service.dto.TransactionTypeEnum;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderStatus;
import com.example.order_service.entity.PaymentMethod;
import com.example.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class OrderPaymentService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentClient paymentClient;
    public ResponseEntity<PaymentUrlResponse> orderPayment(String orderId, String xUserId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId));
        if (!order.getUserId().equals(xUserId)) {
            throw new IllegalArgumentException("Người dùng không được ủy quyền để thanh toán cho đơn hàng này");
        }
        ModelApiResponse response = paymentClient.createPaymentUrl(xUserId, orderId, (long)order.getTotalPrice(), null, order.getPaymentMethod().toString(), TransactionTypeEnum.PAYMENT.toString());
        PaymentUrlResponse paymentUrlResponse = new PaymentUrlResponse();
        paymentUrlResponse.setCode(response.getCode());
        paymentUrlResponse.setMessage(response.getMessage());
        paymentUrlResponse.setData(response.getData());
        return ResponseEntity.ok(paymentUrlResponse);
    }
}
