package com.hcmus.ecommerce_backend.product.service.impl;

import com.hcmus.ecommerce_backend.product.exception.ProductImageAlreadyExistsException;
import com.hcmus.ecommerce_backend.product.exception.ProductImageNotFoundException;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.CreateProductImageRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.UpdateListProductImageRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.UpdateProductImageRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductImageResponse;
import com.hcmus.ecommerce_backend.product.model.entity.ProductImage;
import com.hcmus.ecommerce_backend.product.model.mapper.ProductImageMapper;
import com.hcmus.ecommerce_backend.product.repository.ProductImageRepository;
import com.hcmus.ecommerce_backend.product.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductImageMapper productImageMapper;

    @Override
    public List<ProductImageResponse> getAllProductImages() {
        log.info("ProductImageServiceImpl | getAllProductImages | Retrieving all product images");
        try {
            List<ProductImageResponse> images = productImageRepository.findAll().stream()
                    .map(productImageMapper::toResponse)
                    .collect(Collectors.toList());
            log.info("ProductImageServiceImpl | getAllProductImages | Found {} product images", images.size());
            return images;
        } catch (DataAccessException e) {
            log.error("ProductImageServiceImpl | getAllProductImages | Database error: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public ProductImageResponse getProductImageById(String id) {
        log.info("ProductImageServiceImpl | getProductImageById | id: {}", id);
        try {
            ProductImage productImage = findProductImageById(id);
            log.info("ProductImageServiceImpl | getProductImageById | Product image found with id: {}", id);
            return productImageMapper.toResponse(productImage);
        } catch (ProductImageNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("ProductImageServiceImpl | getProductImageById | Database error for id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ProductImageResponse createProductImage(CreateProductImageRequest request) {
        log.info("ProductImageServiceImpl | createProductImage | Creating product image for productId: {}", request.getProductId());
        try {
            checkProductImageExists(request.getUrl(), request.getProductId());
    
            ProductImage productImage = productImageMapper.toEntity(request);
            ProductImage savedImage = productImageRepository.save(productImage);
            log.info("ProductImageServiceImpl | createProductImage | Created product image with id: {}", savedImage.getId());
            return productImageMapper.toResponse(savedImage);
        } catch (ProductImageAlreadyExistsException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("ProductImageServiceImpl | createProductImage | Database error creating product image '{}': {}", 
                    request.getUrl(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ProductImageResponse updateProductImage(String id, UpdateProductImageRequest request) {
        log.info("ProductImageServiceImpl | updateProductImage | Updating product image with id: {}", id);
        try {
            ProductImage productImage = findProductImageById(id);

            if (!productImage.getUrl().equals(request.getUrl())) {
                checkProductImageExists(request.getUrl(), productImage.getProduct().getId());
            }

            productImageMapper.updateEntity(request, productImage);
            ProductImage updatedImage = productImageRepository.save(productImage);
            log.info("ProductImageServiceImpl | updateProductImage | Updated product image with id: {}", updatedImage.getId());
            return productImageMapper.toResponse(updatedImage);
        } catch (ProductImageNotFoundException | ProductImageAlreadyExistsException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("ProductImageServiceImpl | updateProductImage | Database error updating product image with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteProductImage(String id) {
        log.info("ProductImageServiceImpl | deleteProductImage | Deleting product image with id: {}", id);
        try {
            if (doesProductImageExistById(id)) {
                log.error("ProductImageServiceImpl | deleteProductImage | Product image not found with id: {}", id);
                throw new ProductImageNotFoundException(id);
            }

            productImageRepository.deleteById(id);
            log.info("ProductImageServiceImpl | deleteProductImage | Deleted product image with id: {}", id);
        } catch (ProductImageNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("ProductImageServiceImpl | deleteProductImage | Database error deleting product image with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<ProductImageResponse> getProductImagesByProductId(String productId) {
        log.info("ProductImageServiceImpl | getProductImagesByProductId | Retrieving product images for productId: {}", productId);
        try {
            List<ProductImage> productImages = findProductImagesByProductId(productId);
            log.info("ProductImageServiceImpl | getProductImagesByProductId | Found {} product images for productId: {}", 
                    productImages.size(), productId);
            return productImages.stream()
                    .map(productImageMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (ProductImageNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("ProductImageServiceImpl | getProductImagesByProductId | Database error for productId {}: {}", 
                    productId, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    protected ProductImage findProductImageById(String id) {
        return productImageRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("ProductImageServiceImpl | findProductImageById | Product image not found with id: {}", id);
                    return new ProductImageNotFoundException(id);
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    protected List<ProductImage> findProductImagesByProductId(String productId) {
        List<ProductImage> productImages = productImageRepository.findByProductId(productId);
        if (productImages.isEmpty()) {
            log.error("ProductImageServiceImpl | findProductImagesByProductId | Product images not found for productId: {}", productId);
            throw new ProductImageNotFoundException(productId);
        }
        return productImages;
    }

    @Override
    @Transactional
    public void updateListProductImage(List<UpdateListProductImageRequest> productImages) {
        log.info("ProductImageServiceImpl | updateListProductImage | Processing {} product images", productImages.size());
        try {
            for (UpdateListProductImageRequest imageRequest : productImages) {
                if (imageRequest.getId() == null || imageRequest.getId().isEmpty()) {
                    // Create new product image if ID is empty
                    log.info("ProductImageServiceImpl | updateListProductImage | Creating new product image: {}", imageRequest);
                    ProductImage productImage = productImageMapper.toEntity(imageRequest);
                    productImageRepository.save(productImage);
                } else if (imageRequest.getUrl() == null || imageRequest.getUrl().isEmpty()) {
                    // Delete product image if URL is empty
                    log.info("ProductImageServiceImpl | updateListProductImage | Deleting product image with ID: {}", imageRequest.getId());
                    if (doesProductImageExistById(imageRequest.getId())) {
                        log.warn("ProductImageServiceImpl | updateListProductImage | Product image not found with ID: {}", imageRequest.getId());
                        throw new ProductImageNotFoundException(imageRequest.getId());
                    }
                    productImageRepository.deleteById(imageRequest.getId());
                }
            }
        } catch (ProductImageNotFoundException e) {
            log.error("ProductImageServiceImpl | updateListProductImage | Product image not found: {}", e.getMessage(), e);
            throw e;
        } catch (DataAccessException e) {
            log.error("ProductImageServiceImpl | updateListProductImage | Database error: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    protected boolean doesProductImageExistById(String id) {
        return !productImageRepository.existsById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    protected void checkProductImageExists(String url, String productId) {
        if (productImageRepository.existsByUrlAndProductId(url, productId)) {
            log.error("ProductImageServiceImpl | checkProductImageExists | Product image already exists with URL: {} and productId: {}", url, productId);
            throw new ProductImageAlreadyExistsException(url, productId);
        }
    }
}