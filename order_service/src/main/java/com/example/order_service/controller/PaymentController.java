package com.example.order_service.controller;

import com.example.api.PaymentApi;
import com.example.dto.GetPaymentUrl200Response;
import com.example.order_service.config.VNPayConfig;
import com.example.order_service.entity.PaymentMethod;
import com.example.order_service.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class PaymentController implements PaymentApi {
    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private VNPayConfig vnPayConfig;


    @PostMapping("/create-vnpay-url")
    public ResponseEntity<?> createPaymentUrl(
            @RequestParam String orderId,
            @RequestParam(required = false) String bankCode,
            @RequestHeader(value = "X-User-Id", required = true) String userId,
            @RequestBody PaymentMethod paymentMethod,
            HttpServletRequest request) {
        try {
            String paymentUrl = vnPayService.createPaymentUrl(orderId, bankCode,userId, request, paymentMethod );

            Map<String, Object> response = new HashMap<>();
            response.put("code", "00");
            response.put("message", "success");
            response.put("data", paymentUrl);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("code", "99", "message", e.getMessage()));
        }
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<?> handleVnPayReturn(@RequestParam Map<String, String> params) {
        // 1. Lấy mã hash gửi về
        String vnp_SecureHash = params.get("vnp_SecureHash");

        // 2. Xóa các trường hash để tính toán lại
        Map<String, String> cleanParams = new HashMap<>(params);
        cleanParams.remove("vnp_SecureHashType");
        cleanParams.remove("vnp_SecureHash");

        // 3. Kiểm tra chữ ký (Checksum)
        String signValue = vnPayConfig.hashAllFields(cleanParams);

        if (signValue.equals(vnp_SecureHash)) {
            String responseCode = params.get("vnp_ResponseCode");

            if ("00".equals(responseCode)) {
                // TRƯỜNG HỢP THÀNH CÔNG
                // Lưu ý: Không cập nhật DB ở đây, chỉ trả về thông báo cho Frontend
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Giao dịch thành công",
                        "txnRef", params.get("vnp_TxnRef"),
                        "amount", params.get("vnp_Amount")
                ));
            } else {
                // TRƯỜNG HỢP THẤT BẠI (Người dùng hủy, lỗi thẻ...)
                return ResponseEntity.ok(Map.of(
                        "status", "failed",
                        "message", "Giao dịch không thành công. Mã lỗi: " + responseCode
                ));
            }
        } else {
            // TRƯỜNG HỢP CHỮ KÝ SAI
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Chữ ký không hợp lệ"
            ));
        }
    }
    @GetMapping("/vnpay-ipn")
    public Map<String, String> vnpayIpn(
            @RequestParam Map<String, String> allParams,
            @RequestHeader(value = "X-User-Id", required = true) String userId) {
        return vnPayService.processIpn(allParams, userId);
    }
}
