package com.example.product_service.service;

import com.example.dto.CategoryRequest;
import com.example.dto.CategoryResponse;
import com.example.product_service.entity.Category;
import com.example.product_service.mapper.CategoryMapper;
import com.example.product_service.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(String id) {
        return categoryMapper.toCategoryResponse(Objects.requireNonNull(categoryRepository.findById(id).orElse(null)));
    }

    public void createCategory(String xUserId, CategoryRequest categoryRequest, String xUserRole) {
        Category category = new Category();
        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        categoryRepository.save(category);
    }

    public void updateCategory(String id, String xUserId, CategoryRequest categoryRequest, String xUserRole) {
        Category category = Objects.requireNonNull(categoryRepository.findById(id).orElse(null));
        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        categoryRepository.save(category);
    }

    public void deleteCategory(String id, String xUserId, String xUserRole) {
        categoryRepository.deleteById(id);
    }
}
