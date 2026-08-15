package com.example.user_service.mapper;

import com.example.dto.WalletResponse;
import com.example.dto.WalletTransactionResponse;
import com.example.user_service.entity.Wallet;
import com.example.user_service.entity.WalletTransaction;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;

@Component
public class WalletMapper {

    public WalletResponse toWalletResponse(Wallet wallet) {
        if (wallet == null) {
            return null;
        }
        return new WalletResponse().id(wallet.getId()).balance(wallet.getBalance());
    }

    public WalletTransactionResponse toWalletTransactionResponse(WalletTransaction transaction) {
        if (transaction == null) {
            return null;
        }
        return new WalletTransactionResponse()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType() == null ? null : WalletTransactionResponse.TypeEnum.valueOf(String.valueOf(transaction.getType())))
                .status(transaction.getStatus() == null ? null : WalletTransactionResponse.StatusEnum.valueOf(String.valueOf(transaction.getStatus())))
                .paymentMethod(transaction.getPaymentMethod() == null ? null : WalletTransactionResponse.PaymentMethodEnum.valueOf(String.valueOf(transaction.getPaymentMethod())))
                .createdAt(transaction.getCreatedAt() == null ? null : transaction.getCreatedAt().atOffset(ZoneOffset.UTC));
    }
}
