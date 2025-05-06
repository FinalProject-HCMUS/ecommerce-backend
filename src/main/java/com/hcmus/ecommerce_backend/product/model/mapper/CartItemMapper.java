package com.hcmus.ecommerce_backend.product.model.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmus.ecommerce_backend.product.model.dto.request.CreateCartItemRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateCartItemRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.CartItemResponse;
import com.hcmus.ecommerce_backend.product.model.dto.response.CartItemWithProductResponse;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductResponse;
import com.hcmus.ecommerce_backend.product.model.entity.CartItem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    CartItemResponse toResponse(CartItem cartItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CartItem toEntity(CreateCartItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(UpdateCartItemRequest request, @MappingTarget CartItem cartItem);

    @Mapping(source = "product", target = "product", qualifiedByName = "mapToProductResponse")
    @Mapping(source = "id", target = "id", qualifiedByName = "mapToString")
    @Mapping(source = "color", target = "color", qualifiedByName = "mapToString")
    @Mapping(source = "size", target = "size", qualifiedByName = "mapToString")
    @Mapping(source = "quantity", target = "quantity", qualifiedByName = "mapToInteger")
    @Mapping(source = "userId", target = "userId", qualifiedByName = "mapToString")
    @Mapping(source = "itemId", target = "itemId", qualifiedByName = "mapToString")
    CartItemWithProductResponse toCartItemWithProductReponse(Map<String, Object>cartItemList);

    @Named("mapToProductResponse")
    default ProductResponse mapToProductResponse(Object product) {
        if (product instanceof String) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> productMap = objectMapper.readValue((String) product, Map.class);
                return ProductResponse.builder()
                        .id((String) productMap.get("productId"))
                        .name((String) productMap.get("productName"))
                        .description((String) productMap.get("productDescription"))
                        .cost(((Number) productMap.get("productCost")).doubleValue())
                        .total(((Number) productMap.get("productTotal")).intValue())
                        .price(((Number) productMap.get("productPrice")).doubleValue())
                        .discountPercent(((Number) productMap.get("productDiscountPercent")).doubleValue())
                        .enable((Boolean) productMap.get("productEnable"))
                        .inStock((Boolean) productMap.get("productInStock"))
                        .mainImageUrl((String) productMap.get("productMainImageUrl"))
                        .averageRating(((Number) productMap.get("productAverageRating")).doubleValue())
                        .reviewCount(((Number) productMap.get("productReviewCount")).intValue())
                        .categoryId((String) productMap.get("productCategoryId"))
                        .categoryName((String) productMap.get("productCategoryName"))
                        .createdAt(mapToLocalDateTimeFlexible(productMap.get("productCreatedAt")))
                        .updatedAt(mapToLocalDateTimeFlexible(productMap.get("productUpdatedAt")))
                        .createdBy((String) productMap.get("productCreatedBy"))
                        .updatedBy((String) productMap.get("productUpdatedBy"))
                        .build();
            } catch (Exception e) {
                System.err.println("Error parsing product JSON: " + e.getMessage());
            }
        }
        return null;
    }

    @Named("mapToString")
    default String mapToString(Object value) {
        try {
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    @Named("mapToInteger")
    default Integer mapToInteger(Object value) {
        try {
            return value != null ? Integer.valueOf(value.toString()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    @Named("mapToLocalDateTimeFlexible")
    default java.time.LocalDateTime mapToLocalDateTimeFlexible(Object value) {
        try {
            if (value != null) {
                // Danh sách các định dạng có thể xử lý
                DateTimeFormatter[] formatters = {
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.S]"),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME, 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") 
                };
    
                for (DateTimeFormatter formatter : formatters) {
                    try {
                        return LocalDateTime.parse(value.toString(), formatter);
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing LocalDateTime (flexible): " + value + ", error: " + e.getMessage());
        }
        return null;
    }
}
