package com.example.user_service.service;

import com.example.dto.*;
import com.example.user_service.dto.DepositEvent;
import com.example.user_service.dto.PaymentEvent;
import com.example.user_service.entity.*;
import com.example.user_service.mapper.WalletMapper;
import com.example.user_service.repository.UserProfileRepository;
import com.example.user_service.repository.WalletRepository;
import com.example.user_service.repository.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    private KafkaTemplate<String, Object> kafkaTemplate;
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

    public void updateWalletBalance(UpdateWalletRequest updateWalletRequest) {
        WalletTransaction walletTransaction = walletTransactionRepository.findByTxnRef(updateWalletRequest.getTxnRef())
                .orElseThrow(() -> new RuntimeException("Giao dịch không tìm thấy"));
        Wallet wallet = walletTransaction.getWallet();
        UserProfile userProfile = userProfileRepository.findById(wallet.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng"));        // Implementation for updating wallet balance
        String type = updateWalletRequest.getTransactionType().toString();
        double amount = updateWalletRequest.getAmount();
        if(walletTransaction.getPaymentMethod().equals(PaymentMethod.WALLET)){
            if(type.equals("DEPOSIT")) {
                wallet.setBalance(wallet.getBalance() + amount);
                walletTransaction.setStatus(TransactionStatus.SUCCESS);
                DepositEvent depositEvent = new DepositEvent();
                depositEvent.setUserName(userProfile.getFullName());
                depositEvent.setEmail(userProfile.getEmail());
                depositEvent.setPhoneNumber(userProfile.getPhoneNumber());
                depositEvent.setTransactionId(updateWalletRequest.getTxnRef());
                depositEvent.setAmount(amount);
                depositEvent.setTime(LocalDateTime.now().toString());
                depositEvent.setCurrentBalance(wallet.getBalance());
                kafkaTemplate.send("deposit", depositEvent);
            } else if(type.equals("PAYMENT")) {
                wallet.setBalance(wallet.getBalance() - amount);
                walletTransaction.setStatus(TransactionStatus.SUCCESS);
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
            walletRepository.save(wallet);
        }
        walletTransaction.setStatus(TransactionStatus.SUCCESS);
        walletTransactionRepository.save(walletTransaction);
    }

    public void updateTransactionStatus(String txnRef, UpdateTransactionStatusRequest updateTransactionStatusRequest) {
        WalletTransaction walletTransaction = walletTransactionRepository.findByTxnRef(txnRef)
                .orElseThrow(() -> new RuntimeException("Giao dịch không tìm thấy"));
        walletTransaction.setStatus(TransactionStatus.valueOf(updateTransactionStatusRequest.getStatus().getValue()));
        walletTransactionRepository.save(walletTransaction);
    }

    public ResponseEntity<Void> createWalletTransaction(CreateWalletTransactionRequest request) {
        Wallet wallet = walletRepository.findById(request.getUserId()).orElse(null);
        WalletTransaction walletTransaction = WalletTransaction.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .txnRef(request.getTxnRef())
                .type(TransactionType.valueOf(request.getTransactionType().name()))
                .createdAt(request.getCreatedAt().toLocalDateTime())
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod().name()))
                .status(TransactionStatus.PENDING)
                .wallet(wallet)
                .build();
        walletTransactionRepository.save(walletTransaction);
        return ResponseEntity.ok().build();
    }
}
