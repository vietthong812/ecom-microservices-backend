package com.example.product_service.mapper;

import com.example.dto.ReviewResponse;
import com.example.product_service.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewResponse toReviewRespone(Review review, String userId,String username){
        ReviewResponse reviewResponse = new ReviewResponse();
        reviewResponse.setId(review.getId());
        reviewResponse.setProductId(review.getProduct().getId());
        reviewResponse.setComment(review.getComment());
        reviewResponse.setRating(review.getRating());
        reviewResponse.setUserId(userId);
        reviewResponse.setUserName(username);
        reviewResponse.setCreatedAt(review.getCreatedAt());
        return reviewResponse;
    }
}
