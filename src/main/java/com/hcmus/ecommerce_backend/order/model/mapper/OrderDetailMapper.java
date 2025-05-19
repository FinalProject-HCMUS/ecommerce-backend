package com.hcmus.ecommerce_backend.order.model.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmus.ecommerce_backend.order.model.dto.request.CreateOrderDetailRequest;
import com.hcmus.ecommerce_backend.order.model.dto.request.UpdateOrderDetailRequest;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderDetailResponse;
import com.hcmus.ecommerce_backend.order.model.dto.response.OrderDetailWithProductResponse;
import com.hcmus.ecommerce_backend.order.model.entity.Order;
import com.hcmus.ecommerce_backend.order.model.entity.OrderDetail;
import com.hcmus.ecommerce_backend.product.model.dto.response.ColorResponse;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductResponse;
import com.hcmus.ecommerce_backend.product.model.dto.response.SizeResponse;

@Mapper(componentModel = "spring")
public interface OrderDetailMapper {

    @Mapping(target = "orderId", source = "order", qualifiedByName = "orderToOrderId")
    OrderDetailResponse toResponse(OrderDetail orderDetail);

    @Mapping(source = "product", target = "product", qualifiedByName = "mapToProductResponse")
    @Mapping(source = "productCost", target = "productCost", qualifiedByName = "mapToDouble")
    @Mapping(source = "quantity", target = "quantity", qualifiedByName = "mapToInteger")
    @Mapping(source = "unitPrice", target = "unitPrice", qualifiedByName = "mapToDouble")
    @Mapping(source = "total", target = "total", qualifiedByName = "mapToDouble")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "mapToLocalDateTimeFlexible")
    @Mapping(source = "createdBy", target = "createdBy", qualifiedByName = "mapToString")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "mapToLocalDateTimeFlexible")
    @Mapping(source = "updatedBy", target = "updatedBy", qualifiedByName = "mapToString")
    @Mapping(source = "itemId", target = "itemId", qualifiedByName = "mapToString")
    @Mapping(source = "orderId", target = "orderId", qualifiedByName = "mapToString")
    @Mapping(source = "id", target = "id", qualifiedByName = "mapToString")
    @Mapping(source = "color", target = "color", qualifiedByName = "mapToColorResponse")
    @Mapping(source = "size", target = "size", qualifiedByName = "mapToSizeResponse")
    @Mapping(source = "limitedQuantity", target = "limitedQuantity", qualifiedByName = "mapToInteger")
    OrderDetailWithProductResponse mapToOrderDetailWithProductResponse(Map<String, Object> orderDetail);

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

    @Named("mapToColorResponse")
    default ColorResponse mapToColorResponse(Object color) {
        if (color instanceof String) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> colorMap = objectMapper.readValue((String) color, Map.class);
                return ColorResponse.builder()
                    .id((String) colorMap.get("colorId"))
                    .name((String) colorMap.get("colorName"))
                    .code((String) colorMap.get("colorCode"))
                    .createdAt(mapToLocalDateTimeFlexible(colorMap.get("colorCreatedAt")))
                    .updatedAt(mapToLocalDateTimeFlexible(colorMap.get("colorUpdatedAt")))
                    .createdBy((String) colorMap.get("colorCreatedBy"))
                    .updatedBy((String) colorMap.get("colorUpdatedBy"))
                    .build();
            } catch (Exception e) {
                System.err.println("Error parsing color JSON: " + e.getMessage());
            }
        }
        return null;
    }

    @Named("mapToSizeResponse")
    default SizeResponse mapToSizeResponse(Object size) {
        if (size instanceof String) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> sizeMap = objectMapper.readValue((String) size, Map.class);
                return SizeResponse.builder()
                    .id((String) sizeMap.get("sizeId"))
                    .name((String) sizeMap.get("sizeName"))
                    .minHeight(((Number) sizeMap.get("minHeight")).intValue())
                    .maxHeight(((Number) sizeMap.get("maxHeight")).intValue())
                    .minWeight(((Number) sizeMap.get("minWeight")).intValue())
                    .maxWeight(((Number) sizeMap.get("maxWeight")).intValue())
                    .createdAt(mapToLocalDateTimeFlexible(sizeMap.get("sizeCreatedAt")))
                    .updatedAt(mapToLocalDateTimeFlexible(sizeMap.get("sizeUpdatedAt")))
                    .createdBy((String) sizeMap.get("sizeCreatedBy"))
                    .updatedBy((String) sizeMap.get("sizeUpdatedBy"))
                    .build();
            } catch (Exception e) {
                System.err.println("Error parsing size JSON: " + e.getMessage());
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
    
    @Named("mapToDouble")
    default Double mapToDouble(Object value) {
        try {
            return value != null ? Double.valueOf(value.toString()) : null;
        } catch (NumberFormatException e) {
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

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "order", source = "orderId", qualifiedByName = "orderIdToOrder")
    OrderDetail toEntity(CreateOrderDetailRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "order", source = "orderId", qualifiedByName = "orderIdToOrder")
    void updateEntity(UpdateOrderDetailRequest request, @MappingTarget OrderDetail orderDetail);

    @Named("orderIdToOrder")
    default Order orderIdToOrder(String orderId) {
        if (orderId == null) {
            return null;
        }
        Order order = new Order();
        order.setId(orderId);
        return order;
    }

    @Named("orderToOrderId")
    default String orderToOrderId(Order order) {
        return order.getId();
    }
}
