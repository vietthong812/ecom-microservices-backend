package com.example.order_service.client;

import com.example.order_service.dto.AddressResponse;
import com.example.order_service.dto.UpdateTransactionStatusRequest;
import com.example.order_service.dto.UpdateWalletRequest;
import com.example.order_service.dto.WalletTransactionResponse;
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
    @PostMapping("/api/users/wallet/update")
    void updateWalletBalance(
            @RequestHeader("X-User-Id") String xUserId,
            @RequestBody UpdateWalletRequest request
    );
    @PatchMapping("/api/users/wallet/transactions/{txn_ref}/status")
    void updateTransactionStatus(
            @PathVariable("txn_ref") String txnRef,
            @RequestBody UpdateTransactionStatusRequest request
    );
    @GetMapping("/api/users/addresses/{id}")
    AddressResponse getAddressById(
            @PathVariable("id") String addressId,
            @RequestHeader("X-User-Id") String xUserId
    );
}