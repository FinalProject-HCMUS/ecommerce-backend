package com.hcmus.ecommerce_backend.review.model.mapper;

import com.hcmus.ecommerce_backend.review.model.dto.request.CreateReviewRequest;
import com.hcmus.ecommerce_backend.review.model.dto.response.ReviewResponse;
import com.hcmus.ecommerce_backend.review.model.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    
    ReviewResponse toResponse(Review review);
    
    @Mapping(target = "id", ignore = true)
    Review toEntity(CreateReviewRequest request);
    
}