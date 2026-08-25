package com.example.product_service.service;

import com.example.product_service.dto.OrderItemDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductConsumer {
    @Autowired
    private KafkaTemplate<String,Object> kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProductService productService;

    @KafkaListener(topics = "stock-update", groupId = "product-group")
    public void consumeStockUpdate(String message) {
        try {
            List<OrderItemDTO> items = objectMapper.readValue(
                    message,
                    new TypeReference<List<OrderItemDTO>>() {}
            );
            for (OrderItemDTO item : items) {
                productService.updateStockQuantity(item.getId(), item.getQuantity());
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi xử lý sự kiện cập nhật stock: " + e.getMessage());
        }
    }
}
