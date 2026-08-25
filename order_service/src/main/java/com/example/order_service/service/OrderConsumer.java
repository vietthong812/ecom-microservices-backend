package com.example.order_service.service;

import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderStatus;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {
	private final OrderRepository orderRepository;
	private final KafkaTemplate<String,Object> kafkaTemplate;
	private final ObjectMapper objectMapper;
	private final OrderMapper orderMapper;
	@Transactional
	@KafkaListener(topics = "order_update", groupId = "order-group")
	public void consumeOrderUpdate(String message) {
		try {
			log.info("Nhận được sự kiện cập nhật đơn hàng: {}", message);
			String orderId = objectMapper.readValue(message, String.class);
			Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId));
			order.setStatus(OrderStatus.PAID);
			orderRepository.save(order);
			kafkaTemplate.send("stock-update", order.getItems().stream().map(orderMapper::toOrderItemDTO).toList());
		} catch (Exception e) {
			log.error("Lỗi khi xử lý sự kiện cập nhật đơn hàng: ", e);
		}
	}
}
