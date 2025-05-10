package com.hcmus.ecommerce_backend.product.model.mapper;

    import com.hcmus.ecommerce_backend.product.model.dto.request.product.CreateProductColorSizeRequest;
    import com.hcmus.ecommerce_backend.product.model.dto.request.product.UpdateProductColorSizeRequest;
    import com.hcmus.ecommerce_backend.product.model.dto.response.ProductColorSizeResponse;
    import com.hcmus.ecommerce_backend.product.model.entity.ProductColorSize;
    import org.mapstruct.Mapper;
    import org.mapstruct.Mapping;
    import org.mapstruct.MappingTarget;

    @Mapper(componentModel = "spring", uses = {ProductMapper.class, ColorMapper.class, SizeMapper.class})
    public interface ProductColorSizeMapper {

        @Mapping(target = "product", source = "product", qualifiedBy = {})
        @Mapping(target = "color", source = "color", qualifiedBy = {})
        @Mapping(target = "size", source = "size", qualifiedBy = {})
        ProductColorSizeResponse toResponse(ProductColorSize productColorSize);

        @Mapping(target = "id", ignore = true)
        @Mapping(target = "createdAt", ignore = true)
        @Mapping(target = "createdBy", ignore = true)
        @Mapping(target = "updatedAt", ignore = true)
        @Mapping(target = "updatedBy", ignore = true)
        @Mapping(target = "product.id", source = "productId")
        @Mapping(target = "color.id", source = "colorId")
        @Mapping(target = "size.id", source = "sizeId")
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
    }