package com.example.product_service.controller;

import com.example.api.CategoryApi;
import com.example.dto.CategoryRequest;
import com.example.dto.CategoryResponse;
import com.example.product_service.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CategoryController implements CategoryApi {
    @Autowired
    private CategoryService categoryService;
    @Override
    public ResponseEntity<Void> createCategory(String xUserId, CategoryRequest categoryRequest, String xUserRole) {
        categoryService.createCategory(xUserId, categoryRequest, xUserRole);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteCategory(String id, String xUserId, String xUserRole) {
        categoryService.deleteCategory(id, xUserId, xUserRole);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Override
    public ResponseEntity<CategoryResponse> getCategoryById(String id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @Override
    public ResponseEntity<Void> updateCategory(String id, String xUserId, CategoryRequest categoryRequest, String xUserRole) {
        categoryService.updateCategory(id, xUserId, categoryRequest, xUserRole);
        return ResponseEntity.ok().build();
    }
}
