package com.hcmus.ecommerce_backend.product.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.hcmus.ecommerce_backend.category.model.entity.Category;
import com.hcmus.ecommerce_backend.category.repository.CategoryRepository;
import com.hcmus.ecommerce_backend.product.exception.ProductColorSizeAlreadyExistsException;
import com.hcmus.ecommerce_backend.product.exception.ProductColorSizeNotFoundException;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.CreateProductColorSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.product.UpdateProductColorSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ProductColorSizeResponse;
import com.hcmus.ecommerce_backend.product.model.entity.Product;
import com.hcmus.ecommerce_backend.product.model.entity.ProductColorSize;
import com.hcmus.ecommerce_backend.product.model.mapper.ProductColorSizeMapper;
import com.hcmus.ecommerce_backend.product.repository.ProductColorSizeRepository;
import com.hcmus.ecommerce_backend.product.repository.ProductRepository;
import com.hcmus.ecommerce_backend.product.service.ProductColorSizeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductColorSizeServiceImpl implements ProductColorSizeService{
    
    private final ProductColorSizeRepository productColorSizeRepository;
    private final ProductColorSizeMapper productColorSizeMapper;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductColorSizeResponse> getAllProductColorSizes(Pageable pageable) {
        log.info("ProductColorSizeServiceImpl | getAllProductColorSizes | Retrieving product color sizes with pagination - Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            Page<ProductColorSize> productColorSizePage = productColorSizeRepository.findAll(pageable);
            Page<ProductColorSizeResponse> productColorSizeResponsePage = productColorSizePage.map(productColorSizeMapper::toResponse);
            log.info("ProductColorSizeServiceImpl | getAllProductColorSizes | Found {} product color sizes on page {} of {}",
                    productColorSizeResponsePage.getNumberOfElements(),
                    productColorSizeResponsePage.getNumber() + 1,
                    productColorSizeResponsePage.getTotalPages());
            return productColorSizeResponsePage;
        } catch (DataAccessException e) {
            log.error("ProductColorSizeServiceImpl | getAllProductColorSizes | Database error retrieving paginated product color sizes: {}", e.getMessage(), e);
            return Page.empty(pageable);
        } catch (Exception e) {
            log.error("ProductColorSizeServiceImpl | getAllProductColorSizes | Unexpected error retrieving paginated product color sizes: {}", e.getMessage(), e);
            throw e;
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
    public List<ProductColorSizeResponse> createMultipleProductColorSizes(List<CreateProductColorSizeRequest> requests) {
        log.info("ProductColorSizeServiceImpl | createMultipleProductColorSizes | Creating {} product color sizes", requests.size());
        try {
            // Check for existing combinations first
            for (CreateProductColorSizeRequest request : requests) {
                checkProductColorSizeExists(request.getProductId(), request.getColorId(), request.getSizeId());
            }

            // Convert all to entities
            List<ProductColorSize> productColorSizes = requests.stream()
                    .map(productColorSizeMapper::toEntity)
                    .collect(Collectors.toList());

            // Save all product color sizes
            List<ProductColorSize> savedProductColorSizes = productColorSizeRepository.saveAll(productColorSizes);

            // Convert back to responses
            List<ProductColorSizeResponse> responses = savedProductColorSizes.stream()
                    .map(productColorSizeMapper::toResponse)
                    .collect(Collectors.toList());

            log.info("ProductColorSizeServiceImpl | createMultipleProductColorSizes | Created {} product color sizes",
                    responses.size());
            return responses;
        } catch (ProductColorSizeAlreadyExistsException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("ProductColorSizeServiceImpl | createMultipleProductColorSizes | Database error: {}",
                    e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ProductColorSizeServiceImpl | createMultipleProductColorSizes | Unexpected error: {}",
                    e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ProductColorSizeResponse updateProductColorSize(String id, UpdateProductColorSizeRequest request) {
        log.info("ProductColorSizeServiceImpl | updateProductColorSize | Updating product color size with id: {}", id);
        try {
            ProductColorSize productColorSize = findProductColorSizeById(id);

            // calculate the difference in quantity
            int oldQuantity = productColorSize.getQuantity();
            int newQuantity = request.getQuantity();
            int quantityDifference = newQuantity - oldQuantity;
            
            // Check if the color and size combination already exists for another product
            if (!productColorSize.getColor().getId().equals(request.getColorId()) ||
                !productColorSize.getSize().getId().equals(request.getSizeId())) {
                checkProductColorSizeExists(productColorSize.getProduct().getId(), request.getColorId(), request.getSizeId());
            }

            productColorSizeMapper.updateEntity(request, productColorSize);
            ProductColorSize updatedProductColorSize = productColorSizeRepository.save(productColorSize);

            // update the quantity in ProductColorSize
            Product product = productColorSize.getProduct();
            product.setTotal(product.getTotal() + quantityDifference);
            productRepository.save(product);

            // update the stock in Category

            Category category = product.getCategory();
            category.setStock(category.getStock() + quantityDifference);
            categoryRepository.save(category);

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

    @Override
    @Transactional(readOnly = true)
    public List<ProductColorSizeResponse> getProductColorSizesByProductId(String productId) {
        log.info("ProductColorSizeServiceImpl | getProductColorSizesByProductId | productId: {}", productId);
        try {
            List<ProductColorSizeResponse> productColorSizes = productColorSizeRepository.findByProductId(productId).stream()
                    .map(productColorSizeMapper::toResponse)
                    .collect(Collectors.toList());
            log.info("ProductColorSizeServiceImpl | getProductColorSizesByProductId | Found {} product color sizes for productId {}", productColorSizes.size(), productId);
            return productColorSizes;
        } catch (DataAccessException e) {
            log.error("ProductColorSizeServiceImpl | getProductColorSizesByProductId | Database error for productId {}: {}", productId, e.getMessage(), e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("ProductColorSizeServiceImpl | getProductColorSizesByProductId | Unexpected error for productId {}: {}", productId, e.getMessage(), e);
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
