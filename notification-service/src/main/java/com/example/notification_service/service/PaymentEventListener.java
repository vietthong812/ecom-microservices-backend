package com.example.notification_service.service;

import com.example.notification_service.dto.PaymentEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventListener {
    private static final Logger logger = LoggerFactory.getLogger(PaymentEventListener.class);

    @Autowired
    private EmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @KafkaListener(topics = "payment", groupId = "notification-group")
    public void consumePaymentEvent(String message) {
        try {
            PaymentEvent event = objectMapper.readValue(message, PaymentEvent.class);
            emailService.sendPaymentSuccessEmail(event);
            logger.info("Gửi email thanh toán thành công tới {}", event.getEmail());
        } catch (Exception e) {
            logger.error("Lỗi khi gửi email cho giao dịch ",e);
        }
    }
}
