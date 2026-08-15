package com.example.order_service.dto;

import lombok.Data;

import java.util.List;

@Data
//api call: dto dùng để nhận dữ liệu product từ product service để hiển thị thông tin sản phẩm trong order service
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private Double price;
    private Integer stockQuantity;
    private Double averageRating;
    private String categoryName;
    private List<String> imageUrls;
}
