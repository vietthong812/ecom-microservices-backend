package com.example.user_service.service;

import com.example.dto.UpdateWalletRequest;
import com.example.user_service.dto.DepositEvent;
import com.example.user_service.dto.PaymentEvent;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j

public class UserConsumer {

    private final UserProfileRepository userProfileRepository;
    private final ObjectMapper objectMapper;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletRepository walletRepository;
    private final KafkaTemplate<String,Object> kafkaTemplate;
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
    @KafkaListener(topics = "wallet-update-topic", groupId = "user-group")
    @Transactional
    public void consumeWalletUpdate(String message) {
        try {
            UpdateWalletRequest request = objectMapper.readValue(message, UpdateWalletRequest.class);
            WalletTransaction walletTransaction = walletTransactionRepository.findByTxnRef(request.getTxnRef())
                    .orElseThrow(() -> new RuntimeException("Giao dịch không tìm thấy: " + request.getTxnRef()));

            if (walletTransaction.getStatus() == TransactionStatus.SUCCESS) {
                log.warn("Giao dịch {} đã được xử lý trước đó. Bỏ qua.", request.getTxnRef());
                return;
            }

            Wallet wallet = walletTransaction.getWallet();
            UserProfile userProfile = userProfileRepository.findById(wallet.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));

            double amount = request.getAmount();
            TransactionType type = TransactionType.valueOf(request.getTransactionType().name());

            if (type == TransactionType.DEPOSIT) {
                wallet.setBalance(wallet.getBalance() + amount);

                DepositEvent depositEvent = createDepositEvent(userProfile, request.getTxnRef(), amount, wallet.getBalance());
                kafkaTemplate.send("deposit", depositEvent);
                log.info("Đã gửi DepositEvent cho người dùng: {}", userProfile.getEmail());

            } else if (type == TransactionType.PAYMENT) {
                if(walletTransaction.getPaymentMethod().equals(PaymentMethod.WALLET)) {
                    if (wallet.getBalance() < amount) {
                        throw new RuntimeException("Số dư không đủ để thực hiện thanh toán");
                    }
                    wallet.setBalance(wallet.getBalance() - amount);
                }
                PaymentEvent paymentEvent = createPaymentEvent(userProfile, request.getTxnRef(), amount, wallet.getBalance());
                kafkaTemplate.send("payment", paymentEvent);
                log.info("Đã gửi PaymentEvent cho người dùng: {}", userProfile.getEmail());
            }

            walletRepository.save(wallet);

            walletTransaction.setStatus(TransactionStatus.SUCCESS);
            walletTransactionRepository.save(walletTransaction);

            log.info("Cập nhật ví thành công cho txnRef: {}. Số dư mới: {}", request.getTxnRef(), wallet.getBalance());
            kafkaTemplate.send("order_update", walletTransaction.getOrderId());
        } catch (Exception e) {
            log.error("Lỗi khi xử lý cập nhật ví ", e);
        }
    }

    // Helper tạo DepositEvent
    private DepositEvent createDepositEvent(UserProfile user, String txnRef, double amount, double balance) {
        DepositEvent event = new DepositEvent();
        event.setUserName(user.getFullName());
        event.setEmail(user.getEmail());
        event.setPhoneNumber(user.getPhoneNumber());
        event.setTransactionId(txnRef);
        event.setAmount(amount);
        event.setTime(LocalDateTime.now().toString());
        event.setCurrentBalance(balance);
        return event;
    }

    // Helper tạo PaymentEvent
    private PaymentEvent createPaymentEvent(UserProfile user, String txnRef, double amount, double balance) {
        PaymentEvent event = new PaymentEvent();
        event.setUserName(user.getFullName());
        event.setEmail(user.getEmail());
        event.setPhoneNumber(user.getPhoneNumber());
        event.setTransactionId(txnRef);
        event.setAmount(amount);
        event.setTime(LocalDateTime.now().toString());
        event.setCurrentBalance(balance);
        return event;
    }
}