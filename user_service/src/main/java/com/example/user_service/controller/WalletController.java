package com.example.user_service.controller;

import com.example.api.WalletApi;
import com.example.dto.*;
import com.example.user_service.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WalletController implements WalletApi {
    @Autowired
    private WalletService walletService;

    @Override
    public ResponseEntity<Void> createWalletTransaction(CreateWalletTransactionRequest createWalletTransactionRequest) {
        return walletService.createWalletTransaction(createWalletTransactionRequest);
    }

    @Override
    public ResponseEntity<WalletResponse> getWalletBalance(String xUserId) {
        return new ResponseEntity<>(walletService.getWalletBalance(xUserId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<WalletTransactionResponse> getWalletTransactionByTxnRef(String txnRef) {
        return new ResponseEntity<>(walletService.getWalletTransactionByTxnRef(txnRef), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<WalletTransactionResponse>> getWalletTransactions(String xUserId) {
        return new ResponseEntity<>(walletService.getWalletTransactions(xUserId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateWalletBalance(UpdateWalletRequest updateWalletRequest) {
        walletService.updateWalletBalance(updateWalletRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @Override
    public ResponseEntity<Void> updateTransactionStatus(String txnRef, UpdateTransactionStatusRequest updateTransactionStatusRequest) {
        walletService.updateTransactionStatus(txnRef, updateTransactionStatusRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
