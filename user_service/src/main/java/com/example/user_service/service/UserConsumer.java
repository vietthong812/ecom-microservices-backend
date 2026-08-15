package com.example.user_service.service;

import com.example.user_service.dto.TransactionDTO;
import com.example.user_service.dto.UserRegistrationEvent;
import com.example.user_service.entity.*;
import com.example.user_service.repository.UserProfileRepository;
import com.example.user_service.repository.WalletRepository;
import com.example.user_service.repository.WalletTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j

public class UserConsumer {

    private final UserProfileRepository userProfileRepository;
    private final ObjectMapper objectMapper;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletRepository walletRepository;
    @KafkaListener(topics = "user-registration", groupId = "user-group")
    @Transactional
    public void userRegistrationConsume(String message) {
        try {
            // Deserialize JSON message to UserRegistrationEvent object
            UserRegistrationEvent event = objectMapper.readValue(message, UserRegistrationEvent.class);
            log.info("Nhận được sự kiện đăng ký user: {}", event.getEmail());

            // 1. Tạo UserProfile
            UserProfile profile = UserProfile.builder()
                    .id(event.getUserId())
                    .email(event.getEmail())
                    .fullName(event.getFullName())
                    .phoneNumber(event.getPhoneNumber())
                    .build();

            // 2. Tạo Wallet cho User
            Wallet wallet = Wallet.builder()
                    .userProfile(profile)
                    .balance(0.0)
                    .build();
            profile.setWallet(wallet);

            // 3. Lưu vào DB của User Service
            userProfileRepository.save(profile);
            log.info("Đã khởi tạo Profile và Wallet cho User ID: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Lỗi khi xử lý sự kiện đăng ký user: ", e);
        }
    }

    @KafkaListener(topics = "create-transaction-topic", groupId = "user-group")
    @Transactional
    public void createTransactionConsume(String message){
        try{
            TransactionDTO transactionDTO = objectMapper.readValue(message, TransactionDTO.class);
            Wallet wallet = walletRepository.findById(transactionDTO.getUserId()).orElse(null);
            WalletTransaction walletTransaction = WalletTransaction.builder()
                    .amount(transactionDTO.getAmount())
                    .txnRef(transactionDTO.getTxnRef())
                    .type(TransactionType.PAYMENT)
                    .createdAt(transactionDTO.getCreateAt())
                    .paymentMethod(PaymentMethod.valueOf(transactionDTO.getPaymentMethod()))
                    .wallet(wallet)
                    .build();
            walletTransactionRepository.save(walletTransaction);
        } catch (Exception e) {
            log.error("Lỗi khi xử lý sự kiện tạo giao dịch: ", e);
        }
    }
}