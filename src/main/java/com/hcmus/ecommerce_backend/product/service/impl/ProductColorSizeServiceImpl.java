package com.hcmus.ecommerce_backend.product.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.ecommerce_backend.product.exception.ProductColorSizeNotFoundException;
import com.hcmus.ecommerce_backend.product.exception.ProductImageNotFoundException;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductColorSizeResponse;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductImageResponse;
import com.hcmus.ecommerce_backend.product.model.entity.ProductColorSize;
import com.hcmus.ecommerce_backend.product.model.entity.ProductImage;
import com.hcmus.ecommerce_backend.product.model.mapper.ProductColorSizeMapper;
import com.hcmus.ecommerce_backend.product.repository.ProductColorSizeRepository;
import com.hcmus.ecommerce_backend.product.service.ProductColorSizeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductColorSizeServiceImpl implements ProductColorSizeService{
    
    private final ProductColorSizeRepository productColorSizeRepository;
    private final ProductColorSizeMapper productColorSizeMapper;

    @Override
    public List<ProductColorSizeResponse> getAllProductColorSizes() {
        log.info("ProductColorSizeServiceImpl | getAllProductColorSizes | Retrieving all product_color_sizes");
        try {
            List<ProductColorSizeResponse> productColorSizes = productColorSizeRepository.findAll().stream()
                    .map(productColorSizeMapper::toResponse)
                    .collect(Collectors.toList());
            log.info("ProductColorSizeServiceImpl | getAllProductColorSizes | Found {} product_color_sizes", productColorSizes.size());
            return productColorSizes;
        } catch (DataAccessException e) {
            log.error("ProductColorSizeServiceImpl | getAllProductColorSizes | Database error: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public ProductColorSizeResponse getProductColorSizeById(String id) {
        log.info("ProductColorSizeServiceImpl | getProductColorSizeById | id: {}", id);
        try {
            ProductColorSize productColorSize = findProductColorSizeById(id);
            log.info("ProductColorSizeServiceImpl | getProductColorSizeById | ProductColorSize found: {}", productColorSize.getId());
            return productColorSizeMapper.toResponse(productColorSize);
        } catch (DataAccessException e) {
            log.error("ProductColorSizeServiceImpl | getProductColorSizeById | Database error for id {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ProductColorSizeServiceImpl | getProductColorSizeById | Unexpected error for id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }



    


    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private ProductColorSize findProductColorSizeById(String id) {
        return productColorSizeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("ProductColorSizeServiceImpl | findProductColorSizeById | ProductColorSize not found with id: {}", id);
                    return new ProductColorSizeNotFoundException(id);
                });
    }
}
