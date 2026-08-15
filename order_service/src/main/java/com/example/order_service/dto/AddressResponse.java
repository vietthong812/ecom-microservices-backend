package com.example.order_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
//api call: dto dùng để nhận dữ liệu địa chỉ nhận từ userservice để tạo order
public class AddressResponse {

    private String id;

    private String street;

    private String city;

    private String district;

    private Boolean isDefault;
}
