package com.example.user_service.repository;

import com.example.user_service.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    // Lấy lịch sử giao dịch của ví, sắp xếp theo thời gian mới nhất lên đầu
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(String walletId);

    Optional<WalletTransaction> findByTxnRef(String txnRef);

}