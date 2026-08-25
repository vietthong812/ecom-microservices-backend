package com.example.payment_service.controller;

import com.example.api.VnPayApi;
import com.example.dto.ModelApiResponse;
import com.example.dto.VnPayIpnResponse;
import com.example.dto.VnPayReturnResponse;
import com.example.payment_service.config.VNPayConfig;
import com.example.payment_service.service.VNPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class VNPayController implements VnPayApi {
    @Autowired
    private VNPayConfig vnPayConfig;
    @Autowired
    private VNPayService vnPayService;

    @Override
    public ResponseEntity<ModelApiResponse> createDepositUrl(String xUserId, Long amount, String bankCode) {
        return vnPayService.createDepositUrl(xUserId, amount, bankCode);
    }

    @Override
    public ResponseEntity<ModelApiResponse> createPaymentUrl(String xUserId,String orderId, Long amount, String bankCode, String paymentMethod, String transactionType) {
        return vnPayService.createPaymentUrl(xUserId, orderId ,amount, bankCode, paymentMethod, transactionType);
    }

    @Override
    public ResponseEntity<VnPayReturnResponse> handleVnPayReturn(Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");

        Map<String, String> cleanParams = new HashMap<>(params);
        cleanParams.remove("vnp_SecureHashType");
        cleanParams.remove("vnp_SecureHash");

        // Kiểm tra chữ ký (Checksum)
        String signValue = vnPayConfig.hashAllFields(cleanParams);
        VnPayReturnResponse vnPayReturnResponse = new VnPayReturnResponse();
        if (signValue.equals(vnp_SecureHash)) {
            String responseCode = params.get("vnp_ResponseCode");
            if ("00".equals(responseCode)) {
                vnPayReturnResponse.setMessage("Giao dịch thành công");
                vnPayReturnResponse.setTxnRef(params.get("vnp_TxnRef"));
                vnPayReturnResponse.setAmount(params.get("vnp_Amount"));
                return ResponseEntity.ok(vnPayReturnResponse);
            } else {
                vnPayReturnResponse.setMessage("Giao dịch không thành công. Mã lỗi: " + responseCode);
                return ResponseEntity.ok(vnPayReturnResponse);
            }
        } else {
            vnPayReturnResponse.setMessage("Chữ ký không hợp lệ");
            return ResponseEntity.badRequest().body(vnPayReturnResponse);
        }
    }

    @Override
    public ResponseEntity<Map<String, String>> vnpayIpn(Map<String, String> allParams) {
        return ResponseEntity.ok(vnPayService.processIpn(allParams));
    }
}
