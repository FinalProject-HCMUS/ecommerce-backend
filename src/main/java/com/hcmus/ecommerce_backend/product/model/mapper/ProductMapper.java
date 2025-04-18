package com.hcmus.ecommerce_backend.product.model.mapper;

import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.product.model.dto.request.CreateProductRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateProductRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductResponse;
import com.hcmus.ecommerce_backend.product.model.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "category", target = "categoryId", qualifiedByName = "categoryToCategoryId")
    @Mapping(source = "category", target = "categoryName", qualifiedByName = "categoryToCategoryName")
    ProductResponse toResponse(Product product);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "categoryIdToCategory")
    Product toEntity(CreateProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "categoryIdToCategory")
    void updateEntity(UpdateProductRequest request, @MappingTarget Product product);

    @Named("categoryIdToCategory")
    default Category categoryIdToCategory(String id) {
        if (id == null) {
            return null;
        }
        Category category = new Category();
        category.setId(id); 
        return category;
    }

    @Named("categoryToCategoryId")
    default String categoryToCategoryId(Category category) {
        if (category == null) {
            return null;
        }
        return category.getId(); 
    }

    @Named("categoryToCategoryName")
    default String categoryToCategoryName(Category category) {
        if (category == null) {
            return null;
        }
        return category.getName(); 
    }
}