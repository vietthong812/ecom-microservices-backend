package com.example.order_service.controller;

import com.example.api.OrderApi;
import com.example.dto.CreateOrderRequest;
import com.example.dto.OrderPageResponse;
import com.example.dto.OrderResponse;
import com.example.dto.UpdateOrderStatusRequest;
import com.example.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class OrderController implements OrderApi {
    @Autowired
    private OrderService orderService;
    @Override
    public ResponseEntity<OrderResponse> placeOrder(String xUserId, CreateOrderRequest createOrderRequest) {
        return ResponseEntity.ok(orderService.placeOrder(xUserId, createOrderRequest));
    }
    @Override
    public ResponseEntity<Void> cancelOrder(String id, String xUserId) {
        orderService.cancelOrder(id, xUserId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<OrderPageResponse> getMyOrders(String xUserId, String status, Integer page, Integer size) {
        return ResponseEntity.ok(orderService.getMyOrders(xUserId, status, page, size));
    }

    @Override
    public ResponseEntity<OrderResponse> getOrderById(String id, String xUserId) {
        return ResponseEntity.ok(orderService.getOrderById(id, xUserId));
    }

    @Override
    public ResponseEntity<Void> updateOrderStatus(String id, UpdateOrderStatusRequest updateOrderStatusRequest, String xUserRole) {
        orderService.updateOrderStatus(id, updateOrderStatusRequest, xUserRole);
        return ResponseEntity.noContent().build();
    }
}
