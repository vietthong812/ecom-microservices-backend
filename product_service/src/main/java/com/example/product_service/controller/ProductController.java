package com.example.product_service.controller;

import com.example.api.ProductApi;
import com.example.dto.*;
import com.example.dto.UpdateStockQuantityRequest;
import com.example.product_service.service.ProductSearchService;
import com.example.product_service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductController implements ProductApi {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductSearchService productSearchService;

    @Override
    public ResponseEntity<ProductPageResponse> getAllProducts(String categoryId, Double minPrice, Double maxPrice, Integer page, Integer size, String sort) {
        return new ResponseEntity<>(productService.getAllProducts(categoryId, minPrice, maxPrice, page, size, sort), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ProductResponse> getProductById(String id) {
        return new ResponseEntity<>(productService.getProductById(id), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ProductResponse>> getProductsByIds(List<String> ids) {
        return ResponseEntity.ok(productService.getProductsByIds(ids));
    }

    @Override
    public ResponseEntity<ProductResponse> createProduct(ProductRequest productRequest) {
        return new ResponseEntity<>(productService.createProduct(productRequest), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteProduct(String id) {
        productService.deleteProductById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<List<ProductResponse>> searchProducts(String keyword) {
        return new ResponseEntity<>(productSearchService.searchProducts(keyword), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateStockQuantity(String id, Integer quantity) {
        return productService.updateStockQuantity(id, quantity);
    }

    @Override
    public ResponseEntity<ProductResponse> updateProduct(String id, ProductRequest productRequest) {
        return new ResponseEntity<>(productService.updateProduct(id, productRequest), HttpStatus.OK);
    }
    // Product Review APIs
    @Override
    public ResponseEntity<List<ReviewResponse>> getProductReviews(String id) {
        return new ResponseEntity<>(productService.getReviewsByProductId(id), HttpStatus.OK);
    }
    @Override
    public ResponseEntity<ReviewResponse> addProductReview(String productId, String userId,String userName, ReviewRequest reviewRequest) {
        return new ResponseEntity<>(productService.createReview(productId, userId,userName, reviewRequest), HttpStatus.CREATED);
    }
}
