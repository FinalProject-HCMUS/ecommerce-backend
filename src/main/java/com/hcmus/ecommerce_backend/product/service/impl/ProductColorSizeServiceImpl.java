package com.hcmus.ecommerce_backend.product.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.ecommerce_backend.product.exception.ProductColorSizeAlreadyExistsException;
import com.hcmus.ecommerce_backend.product.exception.ProductColorSizeNotFoundException;
import com.hcmus.ecommerce_backend.product.model.dto.request.CreateProductColorSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateProductColorSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductColorSizeResponse;
import com.hcmus.ecommerce_backend.product.model.entity.ProductColorSize;
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

    @Override
    @Transactional
    public ProductColorSizeResponse createProductColorSize(CreateProductColorSizeRequest request) {
        log.info("ProductColorSizeServiceImpl | createProductColorSize | Creating product color size");
        try {
            checkProductColorSizeExists(request.getProductId(), request.getColorId(), request.getSizeId());

            ProductColorSize productColorSize = productColorSizeMapper.toEntity(request);
            ProductColorSize savedProductColorSize = productColorSizeRepository.save(productColorSize);
            return productColorSizeMapper.toResponse(savedProductColorSize);
        } catch (ProductColorSizeAlreadyExistsException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("ProductColorSizeServiceImpl | createProductColorSize | Database error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ProductColorSizeResponse updateProductColorSize(String id, UpdateProductColorSizeRequest request) {
        log.info("ProductColorSizeServiceImpl | updateProductColorSize | Updating product color size with id: {}", id);
        try {
            ProductColorSize productColorSize = findProductColorSizeById(id);

            if (!productColorSize.getColor().getId().equals(request.getColorId()) ||
                !productColorSize.getSize().getId().equals(request.getSizeId())) {
                checkProductColorSizeExists(productColorSize.getProduct().getId(), request.getColorId(), request.getSizeId());
            }

            productColorSizeMapper.updateEntity(request, productColorSize);
            ProductColorSize updatedProductColorSize = productColorSizeRepository.save(productColorSize);
            return productColorSizeMapper.toResponse(updatedProductColorSize);
        } catch (ProductColorSizeNotFoundException | ProductColorSizeAlreadyExistsException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("ProductColorSizeServiceImpl | updateProductColorSize | Database error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteProductColorSize(String id) {
        log.info("ProductColorSizeServiceImpl | deleteProductColorSize | Deleting product color size with id: {}", id);
        try {
            if (!productColorSizeRepository.existsById(id)) {
                throw new ProductColorSizeNotFoundException(id);
            }
            productColorSizeRepository.deleteById(id);
        } catch (ProductColorSizeNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("ProductColorSizeServiceImpl | deleteProductColorSize | Database error: {}", e.getMessage(), e);
            throw e;
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private void checkProductColorSizeExists(String productId, String colorId, String sizeId) {
        if (productColorSizeRepository.existsByProductIdAndColorIdAndSizeId(productId, colorId, sizeId)) {
            throw new ProductColorSizeAlreadyExistsException(productId, colorId, sizeId);
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
