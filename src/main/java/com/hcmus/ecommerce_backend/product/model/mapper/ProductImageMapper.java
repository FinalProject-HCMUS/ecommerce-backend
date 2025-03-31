package com.hcmus.ecommerce_backend.product.model.mapper;

import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.product.model.dto.request.CreateProductImageRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateProductImageRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductImageResponse;
import com.hcmus.ecommerce_backend.product.model.entity.Product;
import com.hcmus.ecommerce_backend.product.model.entity.ProductImage;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {

    @Mapping(target = "productId", source = "product", qualifiedByName = "productToProductId")
    ProductImageResponse toResponse(ProductImage productImage);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "product", source = "productId", qualifiedByName = "productIdToProduct")
    ProductImage toEntity(CreateProductImageRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "product", source = "productId", qualifiedByName = "productIdToProduct")
    void updateEntity(UpdateProductImageRequest request, @MappingTarget ProductImage productImage);

    @Named("productIdToProduct")
    default Product productIdToProduct(String id) {
        if (id == null) {
            return null;
        }
        Product product = new Product();
        product.setId(id);
        return product;
    }

    @Named("productToProductId")
    default String productToProductId(Product product) {
        if (product == null) {
            return null;
        }
        return product.getId();
    }
}