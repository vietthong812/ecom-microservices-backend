package com.example.product_service.mapper;

import com.example.dto.CategoryResponse;
import com.example.product_service.entity.Category;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Component
public class CategoryMapper {
    public CategoryResponse toCategoryResponse(Category category){
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setId(category.getId());
        categoryResponse.setName(category.getName());
        categoryResponse.setDescription(category.getDescription());
        return categoryResponse;
    }
}
