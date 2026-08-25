package com.example.order_service.mapper;

import com.example.dto.OrderItemResponse;
import com.example.dto.OrderResponse;
import com.example.order_service.dto.OrderItemDTO;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class OrderMapper {

    public OrderResponse toOrderResponse(Order order) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setId(order.getId());
        orderResponse.setUserId(order.getUserId());
        orderResponse.setTotalPrice(order.getTotalPrice());
        orderResponse.setStatus(order.getStatus().name());
        orderResponse.setPaymentMethod(order.getPaymentMethod().name());
        orderResponse.setItems(order.getItems().stream().map(this::toOrderItemResponse).toList());
        orderResponse.setCreatedAt(OffsetDateTime.from(order.getCreatedAt()));
        return orderResponse;
    }

    public OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        OrderItemResponse orderItemResponse = new OrderItemResponse();
        orderItemResponse.setProductId(orderItem.getProductId());
        orderItemResponse.setProductName(orderItem.getProductName());
        orderItemResponse.setQuantity(orderItem.getQuantity());
        orderItemResponse.setPriceAtPurchase(orderItem.getPriceAtPurchase());
        return orderItemResponse;
    }
    public OrderItemDTO toOrderItemDTO(OrderItem orderItem) {
        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setId(orderItem.getProductId());
        orderItemDTO.setQuantity(orderItem.getQuantity());
        return orderItemDTO;
    }
}
