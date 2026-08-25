package com.example.product_service.repository;

import com.example.product_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category,String> {
    @Override
    List<Category> findAll();

    @Override
    Optional<Category> findById(String aLong);

    @Override
    void deleteById(String aLong);
}
