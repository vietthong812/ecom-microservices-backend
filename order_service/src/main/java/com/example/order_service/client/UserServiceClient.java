package com.example.order_service.client;

import com.example.order_service.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "USER-SERVICE"
)
public interface UserServiceClient {
    @GetMapping("/api/users/addresses/{id}")
    AddressResponse getAddressById(
            @PathVariable("id") String addressId,
            @RequestHeader("X-User-Id") String xUserId
    );
}