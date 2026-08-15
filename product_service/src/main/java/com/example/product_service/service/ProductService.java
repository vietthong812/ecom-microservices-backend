package com.example.product_service.service;

import com.example.dto.*;
import com.example.product_service.document.ProductIndex;
import com.example.product_service.mapper.ReviewMapper;
import com.example.product_service.entity.Category;
import com.example.product_service.entity.Product;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.entity.Review;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.repository.ProductSearchRepository;
import com.example.product_service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;           // MySQL
    private final ReviewRepository reviewRepository;             // MySQL
    private final ProductSearchRepository productSearchRepository; // Elasticsearch
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ReviewMapper reviewMapper;
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        // 1. Tìm Category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục sản phẩm"));

        // 2. Lưu vào MySQL
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrls(request.getImageUrls())
                .category(category)
                .averageRating(0.0)
                .build();

        product = productRepository.save(product);
        log.info("Sản phẩm đã được lưu vào MySQL với ID: {}", product.getId());
        ProductIndex productIndex = productMapper.toElasticsearchIndex(product);
        // 3. Đồng bộ sang Elasticsearch
        try {

            productSearchRepository.save(productIndex);
            log.info("Sản phẩm đã được đồng bộ vào Elasticsearch với ID: {}", product.getId());
        } catch (Exception e) {
            // Lưu ý: Nếu ES lỗi, bạn có thể chọn rollback MySQL hoặc chỉ log lỗi
            log.error("Cập nhật sản phẩm trong Elasticsearch thất bại: {}", e.getMessage());
            // throw new RuntimeException("ES Sync Failed"); // Mở dòng này nếu muốn rollback cả MySQL
        }
        return productMapper.toProductRespone(product);
    }

    @Transactional
    public void deleteProductById(String id) {
        // Xóa MySQL
        productRepository.deleteById(id);

        // Xóa Elasticsearch
        productSearchRepository.deleteById(id);
        log.info("Sản phẩm đã được xóa khỏi cả DB và ES: {}", id);
    }

    public ProductResponse getProductById(String id) {
        return productMapper.toProductRespone(productRepository.findById(id).orElse(null));
    }
    public ProductPageResponse getAllProducts(String categoryId, Double minPrice, Double maxPrice, Integer page, Integer size, String sort) {

        // 1. Xử lý Sort object từ string (ví dụ: "price,asc")
        Sort sortObj = Sort.unsorted();
        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            String property = parts[0]; // ví dụ: price
            String direction = parts[1]; // ví dụ: asc
            sortObj = Sort.by(Sort.Direction.fromString(direction), property);
        }

        // 2. Tạo đối tượng Pageable
        Pageable pageable = PageRequest.of(page, size, sortObj);

        // 3. Gọi duy nhất 1 hàm repository (Không cần if-else nữa)
        Page<Product> productPage = productRepository.findProductsWithFilters(categoryId, minPrice, maxPrice, pageable);

        // 4. Chuyển đổi Page sang ProductPageResponse (DTO của OpenAPI)
        ProductPageResponse response = productMapper.toProductPageRespone(productPage);
        response.setTotalPages(productPage.getTotalPages());
        response.setTotalElements((int) productPage.getTotalElements());
        response.setLast(productPage.isLast());

        return response;
    }
    public ProductResponse updateProduct(String id, ProductRequest product){
        return productRepository.findById(id).map(existingProduct -> {
            existingProduct.setName(product.getName());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setStockQuantity(product.getStockQuantity());
            existingProduct.setImageUrls(product.getImageUrls());
            // Cập nhật category nếu cần
            if (product.getCategoryId() != null) {
                existingProduct.setCategory(categoryRepository.findById(product.getCategoryId()).orElse(null));
            }
            Product updatedProduct = productRepository.save(existingProduct);

            // Đồng bộ sang Elasticsearch
            try {
                ProductIndex productIndex = productMapper.toElasticsearchIndex(updatedProduct);
                productSearchRepository.save(productIndex);
                log.info("Sản phẩm đã được cập nhật trong Elasticsearch với ID: {}", updatedProduct.getId());
            } catch (Exception e) {
                log.error("Cập nhật sản phẩm trong Elasticsearch thất bại: {}", e.getMessage());
            }

            return productMapper.toProductRespone(updatedProduct);
        }).orElse(null);
    }


    public List<ReviewResponse> getReviewsByProductId(String productId) {
        return reviewRepository.findByProductId(productId)
                .stream()
                .map(review -> reviewMapper.toReviewRespone(review, review.getUserId(), review.getUserName())).toList();
    }

    public ReviewResponse createReview(String productId, String userId ,String userName,ReviewRequest reviewRequest) {
        Review review = new Review();
        review.setRating(reviewRequest.getRating());
        review.setComment(reviewRequest.getComment());
        review.setUserId(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        review.setProduct(product);
        Review savedReview = reviewRepository.save(review);

        // Cập nhật averageRating cho sản phẩm
        List<Review> reviews = reviewRepository.findByProductId(productId);
        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        product.setAverageRating(averageRating);
        productRepository.save(product);

        // Đồng bộ sang Elasticsearch
        try {
            ProductIndex productIndex = productMapper.toElasticsearchIndex(product);
            productSearchRepository.save(productIndex);
            log.info("Điểm trung bình của sản phẩm {} đã được cập nhật trong Elasticsearch", product.getId());
        } catch (Exception e) {
            log.error("Cập nhật điểm trung bình của sản phẩm {} trong Elasticsearch thất bại: {}", product.getId(), e.getMessage());
        }

        return reviewMapper.toReviewRespone(savedReview,userId,userName);
    }

    public List<ProductResponse> getProductsByIds (List<String> ids){
        List<Product> productList = productRepository.findAllByIdIn(ids);
        return productList.stream().map(productMapper::toProductRespone).collect(Collectors.toList());
    }
}