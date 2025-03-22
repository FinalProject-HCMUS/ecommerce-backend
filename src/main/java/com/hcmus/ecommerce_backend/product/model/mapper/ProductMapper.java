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

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "categoryIdToCategory")
    @Mapping(target = "averageRating", constant = "0.0")
    @Mapping(target = "reviewCount", constant = "0.0")
    @Mapping(target = "createdTime", expression = "java(java.time.LocalDateTime.now().toString())")
    @Mapping(target = "updateTime", expression = "java(java.time.LocalDateTime.now().toString())")
    Product toEntity(CreateProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "categoryIdToCategory")
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updateTime", expression = "java(java.time.LocalDateTime.now().toString())")
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
}