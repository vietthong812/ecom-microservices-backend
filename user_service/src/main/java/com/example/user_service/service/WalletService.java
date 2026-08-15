package com.example.user_service.service;

import com.example.dto.UpdateTransactionStatusRequest;
import com.example.dto.UpdateWalletRequest;
import com.example.dto.WalletResponse;
import com.example.dto.WalletTransactionResponse;
import com.example.user_service.dto.DepositEvent;
import com.example.user_service.dto.PaymentEvent;
import com.example.user_service.entity.*;
import com.example.user_service.mapper.WalletMapper;
import com.example.user_service.repository.UserProfileRepository;
import com.example.user_service.repository.WalletRepository;
import com.example.user_service.repository.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WalletService {

    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private WalletMapper walletMapper;
    @Autowired
    private  KafkaTemplate<String, PaymentEvent> kafkaTemplate; // Inject Kafka
    public WalletResponse getWalletBalance(String userId) {
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        Wallet wallet = userProfile.getWallet();
        return walletMapper.toWalletResponse(wallet);
    }

    public List<WalletTransactionResponse> getWalletTransactions(String userId) {
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        Wallet wallet = userProfile.getWallet();
        return walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId()).stream()
                .map(walletMapper::toWalletTransactionResponse)
                .toList();
    }
    public WalletTransactionResponse getWalletTransactionByTxnRef(String txnRef){
        return walletTransactionRepository.findByTxnRef(txnRef)
                .map(walletMapper::toWalletTransactionResponse)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tìm thấy"));
    }

    public void updateWalletBalance(String xUserId, UpdateWalletRequest updateWalletRequest) {
        UserProfile userProfile = userProfileRepository.findById(xUserId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        Wallet wallet = userProfile.getWallet();
        WalletTransaction walletTransaction = walletTransactionRepository.findByTxnRef(updateWalletRequest.getTxnRef())
                .orElseThrow(() -> new RuntimeException("Giao dịch không tìm thấy"));
        // Implementation for updating wallet balance
        String type = updateWalletRequest.getType().toString();
        double amount = updateWalletRequest.getAmount();
        if(walletTransaction.getPaymentMethod().equals(PaymentMethod.WALLET)){
            if(type.equals("DEPOSIT")) {
                wallet.setBalance(wallet.getBalance() + amount);
            } else if(type.equals("WITHDRAW")) {
                wallet.setBalance(wallet.getBalance() - amount);
            }
            walletRepository.save(wallet);
        }
        walletTransaction.setStatus(TransactionStatus.SUCCESS);
        walletTransactionRepository.save(walletTransaction);

        PaymentEvent paymentEvent = new PaymentEvent();
        paymentEvent.setUserName(userProfile.getFullName());
        paymentEvent.setEmail(userProfile.getEmail());
        paymentEvent.setPhoneNumber(userProfile.getPhoneNumber());
        paymentEvent.setTransactionId(updateWalletRequest.getTxnRef());
        paymentEvent.setAmount(amount);
        paymentEvent.setTime(LocalDateTime.now().toString());
        paymentEvent.setCurrentBalance(wallet.getBalance());

        kafkaTemplate.send("payment", paymentEvent);
    }

    public void updateTransactionStatus(String txnRef, UpdateTransactionStatusRequest updateTransactionStatusRequest) {
        WalletTransaction walletTransaction = walletTransactionRepository.findByTxnRef(txnRef)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tìm thấy"));
        walletTransaction.setStatus(TransactionStatus.valueOf(updateTransactionStatusRequest.getStatus().getValue()));
        walletTransactionRepository.save(walletTransaction);
    }
}
