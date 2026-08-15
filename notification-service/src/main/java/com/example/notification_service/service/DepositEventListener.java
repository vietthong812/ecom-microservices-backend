package com.example.notification_service.service;

import com.example.notification_service.dto.DepositEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DepositEventListener {

    private static final Logger logger = LoggerFactory.getLogger(DepositEventListener.class);

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = "deposit", groupId = "notification-group")
    public void consumeDepositEvent(DepositEvent event) {
        logger.info("Đã nhận DepositEvent từ Kafka: TransactionID = {}, Email = {}",
                event.getTransactionId(), event.getEmail());

        try {
            // Tiến hành gửi Email
            emailService.sendDepositSuccessEmail(event);
            logger.info("Gửi email thành công tới {}", event.getEmail());
        } catch (Exception e) {
            logger.error("Lỗi khi gửi email cho giao dịch {}: {}",
                    event.getTransactionId(), e.getMessage(), e);
        }
    }
}