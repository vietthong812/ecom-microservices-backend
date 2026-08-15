package com.example.order_service.service;

import com.example.order_service.client.UserServiceClient;
import com.example.order_service.config.VNPayConfig;
import com.example.order_service.dto.*;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.PaymentMethod;
import com.example.order_service.repository.OrderItemRepository;
import com.example.order_service.repository.OrderRepository;

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
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final VNPayConfig vnPayConfig;
    private final KafkaTemplate<String, TransactionDTO> kafkaTemplate; // Inject Kafka
    private final UserServiceClient userServiceClient;
    public String createPaymentUrl(String orderId, String bankCode, String userId, HttpServletRequest req, PaymentMethod paymentMethod) throws Exception {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn hàng");
        }
        // Đã sửa lại dùng long để tránh tràn số khi giao dịch lớn
        long amount = (long) (order.getTotalPrice() * 100);

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

        //bắn sự kiện kafka tạo giao dịch thanh toán qua cho user service
        TransactionDTO transactionDTO = TransactionDTO.builder()
                .userId(userId)
                .amount(amount)
                .txnRef(vnp_TxnRef)
                .status(TransactionStatus.PENDING)
                .paymentMethod(String.valueOf(paymentMethod))
                .createAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .build();
        kafkaTemplate.send("create-transaction-topic", transactionDTO);

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
    public Map<String, String> processIpn(Map<String, String> params, String xUserId) {
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
            WalletTransactionResponse transaction = userServiceClient.getWalletTransactionByTxnRef(txnRef);

            if (transaction == null) {
                return Map.of("RspCode", "01", "Message", "Không tìm thấy giao dịch");
            }

            // 3. Kiểm tra số tiền (VNPay gửi số tiền * 100)
            long vnpAmount = Long.parseLong(params.get("vnp_Amount")) / 100;
            if (transaction.getAmount() != vnpAmount) {
                return Map.of("RspCode", "04", "Message", "Số tiền không khớp");
            }

            // 4. Kiểm tra trạng thái giao dịch (Đã confirm chưa?)
            if (transaction.getStatus() != TransactionStatus.PENDING) {
                return Map.of("RspCode", "02", "Message", "Giao dịch đã được xử lý");
            }

            // 5. Cập nhật kết quả
            if ("00".equals(params.get("vnp_ResponseCode"))) {
                // THÀNH CÔNG: Trừ tiền khỏi ví
                UpdateWalletRequest updateWalletRequest = new UpdateWalletRequest();
                updateWalletRequest.setAmount(vnpAmount);
                updateWalletRequest.setTxnRef(txnRef);
                updateWalletRequest.setType(TransactionTypeEnum.valueOf("PAYMENT"));
                userServiceClient.updateWalletBalance(xUserId, updateWalletRequest);
            } else {
                // THẤT BẠI
                userServiceClient.updateTransactionStatus(txnRef, new UpdateTransactionStatusRequest(TransactionStatus.FAILED));
            }
            return Map.of("RspCode", "00", "Message", "Xác nhận thành công");

        } catch (Exception e) {
            return Map.of("RspCode", "99", "Message", "Lỗi không xác định");
        }
    }
}