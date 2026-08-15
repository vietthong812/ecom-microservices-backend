package com.example.product_service.mapper;

import com.example.dto.ProductPageResponse;
import com.example.dto.ProductResponse;
import com.example.product_service.document.ProductIndex;
import com.example.product_service.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Component
public class ProductMapper {
    // Chuyển từ Entity (MySQL) sang Document (Elasticsearch)
    public ProductIndex toElasticsearchIndex(Product product) {
        return ProductIndex.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .categoryName(product.getCategory().getName()) // Lấy tên danh mục
                .averageRating(product.getAverageRating())
                .thumbnail(product.getImageUrls().isEmpty() ? null : product.getImageUrls().get(0))
                .build();
    }
    public ProductResponse toResponse(ProductIndex index) {
        ProductResponse response = new ProductResponse();
        response.setId(index.getId());
        response.setName(index.getName());
        response.setDescription(index.getDescription());
        response.setPrice(index.getPrice());
        response.setStockQuantity(index.getStockQuantity());
        response.setAverageRating(index.getAverageRating());
        response.setCategoryName(index.getCategoryName());
        if (index.getThumbnail() != null) {
            response.setImageUrls(java.util.List.of(index.getThumbnail()));
        }
        return response;
    }

    public ProductResponse toProductRespone(Product product){
        if (product == null) {
            return null;
        }

        ProductResponse response = new ProductResponse();
        if (product.getId() != null) {
            response.setId(product.getId());
        }
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        if (product.getAverageRating() != null) {
            response.setAverageRating(product.getAverageRating());
        }
        if (product.getCategory() != null) {
            response.setCategoryName(product.getCategory().getName());
        }
        if (product.getImageUrls() != null) {
            response.setImageUrls(product.getImageUrls());
        }
        return response;
    }

    public ProductPageResponse toProductPageRespone(Page<Product> productPage){
        Page<ProductResponse> productResponsePage=productPage.map(this::toProductRespone);
        ProductPageResponse response = new ProductPageResponse();
        response.setContent(productResponsePage.getContent());
        response.setTotalPages(productResponsePage.getTotalPages());
        response.setTotalElements((int)productResponsePage.getTotalElements());
        response.setLast(productResponsePage.isLast());
        return response;

    }
}
