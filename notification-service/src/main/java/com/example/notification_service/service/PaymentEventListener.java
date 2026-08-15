package com.example.notification_service.service;

import com.example.notification_service.dto.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {
    private static final Logger logger = LoggerFactory.getLogger(PaymentEventListener.class);

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = "payment", groupId = "notification-group")
    public void consumePaymentEvent(PaymentEvent event) {
        logger.info("Đã nhận PaymentEvent từ Kafka: TransactionID = {}, Email = {}",
                event.getTransactionId(), event.getEmail());

        try {
            emailService.sendPaymentSuccessEmail(event);
            logger.info("Gửi email thanh toán thành công tới {}", event.getEmail());
        } catch (Exception e) {
            logger.error("Lỗi khi gửi email cho giao dịch {}: {}",
                    event.getTransactionId(), e.getMessage(), e);
        }
    }
}
