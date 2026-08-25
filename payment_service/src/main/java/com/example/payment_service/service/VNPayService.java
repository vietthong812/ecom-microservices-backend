package com.example.payment_service.service;

import com.example.dto.ModelApiResponse;
import com.example.payment_service.client.UserServiceClient;
import com.example.payment_service.config.VNPayConfig;
import com.example.payment_service.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class VNPayService {
    @Autowired
    private VNPayConfig vnPayConfig;
    @Autowired
    private KafkaTemplate<Object, UpdateWalletRequest> kafkaTemplate;
    @Autowired
    private UserServiceClient userServiceClient;
    public ArrayList<String> createPaymentUrlParams(Long amountInput, String bankCode){
        try{
            String vnp_Version = "2.1.0";
            String vnp_Command = "pay";
            String orderType = "other";

            long amount = amountInput * 100;

            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String vnp_TxnRef = vnPayConfig.getRandomNumber(8);
            String vnp_IpAddr = vnPayConfig.getIpAddress(request);
            String vnp_TmnCode = vnPayConfig.getVnp_TmnCode();
            String vnp_Amount = String.valueOf(amount);
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", vnp_Amount);
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
            String createdUrl= vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
            ArrayList<String> result= new ArrayList<>();
            result.add(vnp_Amount);
            result.add(vnp_TxnRef);
            result.add(createdUrl);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public ResponseEntity<ModelApiResponse> createDepositUrl(String xUserId, Long amount, String bankCode) {
        try {
            ArrayList<String> params = createPaymentUrlParams(amount,bankCode);
            //bắn sự kiện kafka tạo giao dịch thanh toán qua cho user service
            CreateWalletTransactionRequest createWalletTransactionRequest = CreateWalletTransactionRequest.builder()
                    .userId(xUserId)
                    .amount(Double.parseDouble(params.get(0))/100)
                    .txnRef(params.get(1))
                    .status(TransactionStatus.PENDING)
                    .paymentMethod("WALLET")
                    .transactionType("DEPOSIT")
                    .createdAt(OffsetDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                    .build();
            userServiceClient.createWalletTransaction(createWalletTransactionRequest);
            ModelApiResponse modelApiResponse = new ModelApiResponse();
            modelApiResponse.setCode("00");
            modelApiResponse.setMessage("success");
            modelApiResponse.setData(params.get(2));
            return ResponseEntity.ok(modelApiResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<ModelApiResponse> createPaymentUrl(String userId, String orderId, Long amountInput, String bankCode, String paymentMethod, String transactionType) {
        try {
            ArrayList<String> params = createPaymentUrlParams(amountInput,bankCode);
            //bắn sự kiện kafka tạo giao dịch thanh toán qua cho user service
            CreateWalletTransactionRequest createWalletTransactionRequest = CreateWalletTransactionRequest.builder()
                    .userId(userId)
                    .orderId(orderId)
                    .amount(Double.parseDouble(params.get(0))/100)
                    .txnRef(params.get(1))
                    .status(TransactionStatus.PENDING)
                    .paymentMethod(String.valueOf(paymentMethod))
                    .transactionType(String.valueOf(transactionType))
                    .createdAt(OffsetDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                    .build();
            userServiceClient.createWalletTransaction(createWalletTransactionRequest);
            ModelApiResponse modelApiResponse = new ModelApiResponse();
            modelApiResponse.setCode("00");
            modelApiResponse.setMessage("success");
            modelApiResponse.setData(params.get(2));
            return ResponseEntity.ok(modelApiResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public Map<String, String> processIpn(Map<String, String> params) {
        Map<String, String> response = new HashMap<>();
        try {
            String vnp_SecureHash = params.get("vnp_SecureHash");
            params.remove("vnp_SecureHashType");
            params.remove("vnp_SecureHash");

            String signValue = vnPayConfig.hashAllFields(params);

            if (!signValue.equals(vnp_SecureHash)) {
                return Map.of("RspCode", "97", "Message", "Chữ ký không hợp lệ");
            }

            // Tìm giao dịch trong DB (vnp_TxnRef)
            String txnRef = params.get("vnp_TxnRef");
            WalletTransactionResponse transaction = userServiceClient.getWalletTransactionByTxnRef(txnRef);
            System.out.println("Transaction: " + transaction);
            if (transaction == null) {
                return Map.of("RspCode", "01", "Message", "Giao dịch không tìm thấy");
            }

            // Kiểm tra số tiền (VNPay gửi số tiền * 100)
            long vnpAmount = Long.parseLong(params.get("vnp_Amount")) / 100;
            if (transaction.getAmount() != vnpAmount) {
                return Map.of("RspCode", "04", "Message", "Số tiền không khớp");
            }

            // Kiểm tra trạng thái đơn hàng
            if (transaction.getStatus() != TransactionStatus.PENDING) {
                return Map.of("RspCode", "02", "Message", "Giao dịch đã được xử lý");
            }

            // Cập nhật kết quả
            if ("00".equals(params.get("vnp_ResponseCode"))) {
                if (transaction.getType().equals(TransactionType.DEPOSIT)) {
                    UpdateWalletRequest updateWalletRequest = new UpdateWalletRequest();
                    updateWalletRequest.setAmount(vnpAmount);
                    updateWalletRequest.setTxnRef(txnRef);
                    updateWalletRequest.setPaymentMethod(PaymentMethod.WALLET);
                    updateWalletRequest.setTransactionType(TransactionType.DEPOSIT);
                    kafkaTemplate.send("wallet-update-topic",updateWalletRequest);
                }
                if (transaction.getType().equals(TransactionType.PAYMENT)) {
                    if (transaction.getPaymentMethod().equals(PaymentMethod.WALLET)) {
                        UpdateWalletRequest updateWalletRequest = new UpdateWalletRequest();
                        updateWalletRequest.setAmount(vnpAmount);
                        updateWalletRequest.setTxnRef(txnRef);
                        updateWalletRequest.setTransactionType(TransactionType.PAYMENT);
                        updateWalletRequest.setPaymentMethod(PaymentMethod.WALLET);
                        kafkaTemplate.send("wallet-update-topic",updateWalletRequest);
                    }
                    else if (transaction.getPaymentMethod().equals(PaymentMethod.VNPAY)) {
                        UpdateWalletRequest updateWalletRequest = new UpdateWalletRequest();
                        updateWalletRequest.setAmount(vnpAmount);
                        updateWalletRequest.setTxnRef(txnRef);
                        updateWalletRequest.setTransactionType(TransactionType.PAYMENT);
                        updateWalletRequest.setPaymentMethod(PaymentMethod.VNPAY);
                        kafkaTemplate.send("wallet-update-topic",updateWalletRequest);
                    }
                }
            } else {
                userServiceClient.updateTransactionStatus(txnRef, new UpdateTransactionStatusRequest(TransactionStatus.FAILED));
            }
            return Map.of("RspCode", "00", "Message", "Xác nhận thành công");

        } catch (Exception e) {
            return Map.of("RspCode", "99", "Message", "Lỗi không xác định");
        }
    }


}

