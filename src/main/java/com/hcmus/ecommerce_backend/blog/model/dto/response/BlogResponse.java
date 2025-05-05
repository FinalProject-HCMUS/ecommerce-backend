package com.hcmus.ecommerce_backend.blog.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating a new blog")
public class BlogResponse {

    @Schema(description = "Unique identifier of the blog", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @Schema(description = "Title of the fashion blog", example = "Top 10 Summer Outfits for 2024")
    private String title;

    @Schema(description = "Content of the blog", example = "Summer 2024 is all about vibrant colors and airy fabrics...")
    private String content;

    @Schema(description = "Image URL for the blog", example = "https://fashionstore.com/blog/summer-trends.jpg")
    private String image;

    @Schema(description = "Date and time when the blog was created")
    private String createdAt;

    @Schema(description = "Date and time when the blog was last updated")
    private String updatedAt;

    @Schema(description = "User who created the blog", example = "admin")
    private String createdBy;

    @Schema(description = "User who last updated the blog", example = "admin")
    private String updatedBy;
}
