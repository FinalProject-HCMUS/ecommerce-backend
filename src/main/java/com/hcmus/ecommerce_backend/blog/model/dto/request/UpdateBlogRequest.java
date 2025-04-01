package com.hcmus.ecommerce_backend.blog.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating a new blog")
public class UpdateBlogRequest {
        
    @NotBlank(message = "Title cannot be blank")
    @Schema(description = "Title of the fashion blog", example = "Top 10 Summer Outfits for 2024")
    private String title;

    @NotBlank(message = "Content cannot be blank")
    @Schema(description = "Content of the blog", example = "Summer 2024 is all about vibrant colors and airy fabrics...")
    private String content;

    @Schema(description = "Image URL for the blog", example = "https://fashionstore.com/blog/summer-trends.jpg")
    private String image;

    @NotNull(message = "User ID cannot be null")
    @Schema(description = "ID of the admin or shop owner creating the blog", example = "5f3a2b8c-1d9e-4e6a-9238-6b7f4b2c9d1f")
    private String userId;
}
