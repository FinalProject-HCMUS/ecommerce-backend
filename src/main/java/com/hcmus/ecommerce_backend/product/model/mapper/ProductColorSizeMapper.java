package com.hcmus.ecommerce_backend.product.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import com.hcmus.ecommerce_backend.product.model.dto.request.product.CreateProductColorSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.UpdateProductColorSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductColorSizeResponse;
import com.hcmus.ecommerce_backend.product.model.entity.Color;
import com.hcmus.ecommerce_backend.product.model.entity.Product;
import com.hcmus.ecommerce_backend.product.model.entity.ProductColorSize;
import com.hcmus.ecommerce_backend.product.model.entity.Size;

@Mapper(componentModel = "spring")
public interface ProductColorSizeMapper {

    @Mapping(target = "productId", source = "product", qualifiedByName = "productToProductId")
    @Mapping(target = "colorId", source = "color", qualifiedByName = "colorToColorId")
    @Mapping(target = "sizeId", source = "size", qualifiedByName = "sizeToSizeId")
    ProductColorSizeResponse toResponse(ProductColorSize productColorSize);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "product", source = "productId", qualifiedByName = "productIdToProduct")
    @Mapping(target = "color", source = "colorId", qualifiedByName = "colorIdToColor")
    @Mapping(target = "size", source = "sizeId", qualifiedByName = "sizeIdToSize")
    ProductColorSize toEntity(CreateProductColorSizeRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "color", ignore = true)
    @Mapping(target = "size", ignore = true)
    void updateEntity(UpdateProductColorSizeRequest request, @MappingTarget ProductColorSize productColorSize);


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

    @Named("colorIdToColor")
    default Color colorIdToColor(String id) {
        if (id == null) {
            return null;
        }
        Color color = new Color();
        color.setId(id);
        return color;
    }

    @Named("colorToColorId")
    default String colorToColorId(Color color) {
        if (color == null) {
            return null;
        }
        return color.getId();
    }

    @Named("sizeIdToSize")
    default Size sizeIdToSize(String id) {
        if (id == null) {
            return null;
        }
        Size size = new Size();
        size.setId(id);
        return size;
    }

    @Named("sizeToSizeId")
    default String sizeToSizeId(Size size) {
        if (size == null) {
            return null;
        }
        return size.getId();
    }
}
