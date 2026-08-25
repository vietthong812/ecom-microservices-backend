package com.example.payment_service.client;

import com.example.payment_service.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "USER-SERVICE"
)
public interface UserServiceClient {

    @GetMapping("/api/users/wallet/transactions/{txn_ref}")
    WalletTransactionResponse getWalletTransactionByTxnRef(
            @PathVariable("txn_ref") String txnRef
    );

    @PatchMapping("/api/users/wallet/transactions/{txn_ref}/status")
    void updateTransactionStatus(
            @PathVariable("txn_ref") String txnRef,
            @RequestBody UpdateTransactionStatusRequest request
    );
    @PostMapping("/api/users/wallet/transactions")
    void createWalletTransaction(
            @RequestBody CreateWalletTransactionRequest request
    );
}