package com.example.order_service.client;

import com.example.order_service.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "product-service")
public interface ProductServiceClient {
    @PostMapping("/api/products/batch")
    List<ProductResponse> getProductsByIds(@RequestBody List<String> ids);
}