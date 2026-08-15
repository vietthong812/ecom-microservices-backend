package com.example.user_service.service;

import com.example.user_service.config.VNPayConfig;
import com.example.user_service.dto.DepositEvent;
import com.example.user_service.entity.*;
import com.example.user_service.repository.WalletRepository;
import com.example.user_service.repository.WalletTransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VNPayService {
    private final WalletRepository walletRepository;
    private final VNPayConfig vnPayConfig;
    private final WalletTransactionRepository walletTransactionRepository;
    private final KafkaTemplate<String, DepositEvent> kafkaTemplate;

    public String createPaymentUrl(long amountInput, String bankCode,String userId, HttpServletRequest req) throws Exception {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";

        long amount = amountInput * 100;

        String vnp_TxnRef = vnPayConfig.getRandomNumber(8);
        String vnp_IpAddr = vnPayConfig.getIpAddress(req);
        String vnp_TmnCode = vnPayConfig.getVnp_TmnCode();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnp_ReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");

        LocalDateTime now = LocalDateTime.now(zone);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        String vnp_CreateDate = now.format(formatter);
        String vnp_ExpireDate = now.plusMinutes(15).format(formatter);
        
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        //lưu giao dịch vào DB với trạng thái PENDING
        WalletTransaction walletTransaction = new WalletTransaction();
            walletTransaction.setWallet(walletRepository.findById(userId).orElseThrow(() -> new RuntimeException("Wallet not found for userId: " + userId)));
            walletTransaction.setTxnRef(vnp_TxnRef);
            walletTransaction.setAmount((double)amountInput);
        DateTimeFormatter dbFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime createdAt = LocalDateTime.parse(vnp_CreateDate, dbFormatter);
            walletTransaction.setCreatedAt(createdAt);
            walletTransaction.setType(TransactionType.valueOf("DEPOSIT"));
            walletTransaction.setPaymentMethod(PaymentMethod.VNPAY);
            walletTransactionRepository.save(walletTransaction);
        // Sắp xếp tham số và build URL
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = vnPayConfig.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        return vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
    }
    @Transactional
    public Map<String, String> processIpn(Map<String, String> params) {
        Map<String, String> response = new HashMap<>();
        try {
            // 1. Kiểm tra Checksum (Chữ ký)
            String vnp_SecureHash = params.get("vnp_SecureHash");
            params.remove("vnp_SecureHashType");
            params.remove("vnp_SecureHash");

            // Hàm hashAllFields phải sắp xếp các tham số theo Alphabet (Dùng TreeMap)
            String signValue = vnPayConfig.hashAllFields(params);

            if (!signValue.equals(vnp_SecureHash)) {
                return Map.of("RspCode", "97", "Message", "Chữ ký không hợp lệ");
            }

            // 2. Tìm giao dịch trong DB (vnp_TxnRef)
            String txnRef = params.get("vnp_TxnRef");
            WalletTransaction transaction = walletTransactionRepository.findByTxnRef(txnRef)
                    .orElse(null);

            if (transaction == null) {
                return Map.of("RspCode", "01", "Message", "Giao dịch không tìm thấy");
            }

            // 3. Kiểm tra số tiền (VNPay gửi số tiền * 100)
            long vnpAmount = Long.parseLong(params.get("vnp_Amount")) / 100;
            if (transaction.getAmount() != vnpAmount) {
                return Map.of("RspCode", "04", "Message", "Số tiền không khớp");
            }

            // 4. Kiểm tra trạng thái đơn hàng (Đã confirm chưa?)
            if (transaction.getStatus() != TransactionStatus.PENDING) {
                return Map.of("RspCode", "02", "Message", "Giao dịch đã được xử lý");
            }

            // 5. Cập nhật kết quả
            if ("00".equals(params.get("vnp_ResponseCode"))) {
                // THÀNH CÔNG: Cộng tiền vào ví
                Wallet wallet = transaction.getWallet();
                wallet.setBalance(wallet.getBalance() + vnpAmount);
                transaction.setStatus(TransactionStatus.SUCCESS);
                walletRepository.save(wallet);
                // Bắn sự kiện nạp tiền thành công lên Kafka

                DepositEvent depositEvent = new DepositEvent();

                DateTimeFormatter formatter =
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                depositEvent.setTime(
                        transaction.getCreatedAt().format(formatter)
                );
                depositEvent.setEmail(wallet.getUserProfile().getEmail());
                depositEvent.setUserName(wallet.getUserProfile().getFullName());
                depositEvent.setPhoneNumber(wallet.getUserProfile().getPhoneNumber());
                depositEvent.setTransactionId(transaction.getTxnRef());
                depositEvent.setAmount(vnpAmount);
                depositEvent.setCurrentBalance(wallet.getBalance());
                kafkaTemplate.send("deposit", depositEvent);

            } else {
                // THẤT BẠI
                transaction.setStatus(TransactionStatus.FAILED);
            }

            walletTransactionRepository.save(transaction);
            return Map.of("RspCode", "00", "Message", "Xác nhận thành công");

        } catch (Exception e) {
            return Map.of("RspCode", "99", "Message", "Lỗi không xác định");
        }
    }
}