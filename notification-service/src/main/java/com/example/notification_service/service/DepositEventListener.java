package com.example.notification_service.service;

import com.example.notification_service.dto.DepositEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class DepositEventListener {

    private static final Logger logger = LoggerFactory.getLogger(DepositEventListener.class);

    @Autowired
    private EmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @KafkaListener(topics = "deposit", groupId = "notification-group")
    public void consumeDepositEvent(String message) {
        try {
            // Tiến hành gửi Email
            DepositEvent event = objectMapper.readValue(message, DepositEvent.class);
            emailService.sendDepositSuccessEmail(event);
            logger.info("Gửi email thành công tới {}", event.getEmail());
        } catch (Exception e) {
            logger.error("Lỗi khi gửi email cho giao dịch", e);
        }
    }
}