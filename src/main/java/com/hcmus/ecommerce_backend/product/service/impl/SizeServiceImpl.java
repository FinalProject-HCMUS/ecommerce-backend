package com.hcmus.ecommerce_backend.product.service.impl;

import com.hcmus.ecommerce_backend.product.exception.SizeAlreadyExistsException;
import com.hcmus.ecommerce_backend.product.exception.SizeNotFoundException;
import com.hcmus.ecommerce_backend.product.model.dto.request.size.CreateSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.size.UpdateSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.SizeResponse;
import com.hcmus.ecommerce_backend.product.model.entity.Size;
import com.hcmus.ecommerce_backend.product.model.mapper.SizeMapper;
import com.hcmus.ecommerce_backend.product.repository.SizeRepository;
import com.hcmus.ecommerce_backend.product.service.SizeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SizeServiceImpl implements SizeService {

    private final SizeRepository sizeRepository;
    private final SizeMapper sizeMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<SizeResponse> searchSizes(Pageable pageable, String keyword,
                                     Integer minHeight, Integer maxHeight,
                                     Integer minWeight, Integer maxWeight) {
        log.info("SizeServiceImpl | searchSizes | Retrieving sizes with filtering - " +
                "Page: {}, Size: {}, Sort: {}, Keyword: {}, MinHeight: {}, MaxHeight: {}, MinWeight: {}, MaxWeight: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(),
                keyword, minHeight, maxHeight, minWeight, maxWeight);

        try {
            // Sanitize empty keyword to null for consistent query handling
            String sanitizedKeyword = (keyword != null && keyword.trim().isEmpty()) ? null : keyword;

            // Log actual values being used for the query
            log.debug("SizeServiceImpl | searchSizes | Executing query with parameters: " +
                    "keyword={}, minHeight={}, maxHeight={}, minWeight={}, maxWeight={}",
                    sanitizedKeyword, minHeight, maxHeight, minWeight, maxWeight);

            Page<Size> sizePage = sizeRepository.searchSizes(
                    sanitizedKeyword, minHeight, maxHeight, minWeight, maxWeight, pageable);

            Page<SizeResponse> sizeResponsePage = sizePage.map(sizeMapper::toResponse);

            log.info("SizeServiceImpl | searchSizes | Found {} sizes on page {} of {}",
                    sizeResponsePage.getNumberOfElements(),
                    sizeResponsePage.getNumber() + 1,
                    sizeResponsePage.getTotalPages());

            return sizeResponsePage;
        } catch (DataAccessException e) {
            log.error("SizeServiceImpl | searchSizes | Database error retrieving filtered sizes: {}", e.getMessage(), e);
            return Page.empty(pageable);
        } catch (Exception e) {
            log.error("SizeServiceImpl | searchSizes | Unexpected error retrieving filtered sizes: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public SizeResponse getSizeById(String id) {
        log.info("SizeServiceImpl | getSizeById | id: {}", id);
        try {
            Size size = findSizeById(id);
            log.info("SizeServiceImpl | getSizeById | Size found: {}", size.getName());
            return sizeMapper.toResponse(size);
        } catch (SizeNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("SizeServiceImpl | getSizeById | Database error for id {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("SizeServiceImpl | getSizeById | Unexpected error for id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public SizeResponse createSize(CreateSizeRequest request) {
        log.info("SizeServiceImpl | createSize | Creating size with name: {}", request.getName());
        try {
            checkSizeNameExists(request.getName());

            Size size = sizeMapper.toEntity(request);

            Size savedSize = sizeRepository.save(size);
            log.info("SizeServiceImpl | createSize | Created size with id: {}", savedSize.getId());
            return sizeMapper.toResponse(savedSize);
        } catch (SizeAlreadyExistsException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("SizeServiceImpl | createSize | Database error creating size '{}': {}", 
                    request.getName(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("SizeServiceImpl | createSize | Unexpected error creating size '{}': {}", 
                    request.getName(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public List<SizeResponse> createMultipleSizes(List<CreateSizeRequest> requests) {
        log.info("SizeServiceImpl | createMultipleSizes | Creating {} sizes", requests.size());
        try {
            // Check if any size names already exist
            for (CreateSizeRequest request : requests) {
                checkSizeNameExists(request.getName());
            }

            // Convert all to entities
            List<Size> sizes = requests.stream()
                    .map(sizeMapper::toEntity)
                    .collect(Collectors.toList());

            // Save all sizes
            List<Size> savedSizes = sizeRepository.saveAll(sizes);

            // Convert back to responses
            List<SizeResponse> responses = savedSizes.stream()
                    .map(sizeMapper::toResponse)
                    .collect(Collectors.toList());

            log.info("SizeServiceImpl | createMultipleSizes | Created {} sizes", responses.size());
            return responses;
        } catch (SizeAlreadyExistsException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("SizeServiceImpl | createMultipleSizes | Database error creating multiple sizes: {}",
                    e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("SizeServiceImpl | createMultipleSizes | Unexpected error creating multiple sizes: {}",
                    e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public SizeResponse updateSize(String id, UpdateSizeRequest request) {
        log.info("SizeServiceImpl | updateSize | Updating size with id: {}", id);
        try {
            Size size = findSizeById(id);

            if (!size.getName().equals(request.getName())) {
                checkSizeNameExists(request.getName());
            }

            sizeMapper.updateEntity(request, size);
            Size updatedSize = sizeRepository.save(size);
            log.info("SizeServiceImpl | updateSize | Updated size with id: {}", updatedSize.getId());
            return sizeMapper.toResponse(updatedSize);
        } catch (SizeNotFoundException | SizeAlreadyExistsException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("SizeServiceImpl | updateSize | Database error updating size with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("SizeServiceImpl | updateSize | Unexpected error updating size with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteSize(String id) {
        log.info("SizeServiceImpl | deleteSize | Deleting size with id: {}", id);
        try {
            if (!doesSizeExistById(id)) {
                log.error("SizeServiceImpl | deleteSize | Size not found with id: {}", id);
                throw new SizeNotFoundException(id);
            }

            sizeRepository.deleteById(id);
            log.info("SizeServiceImpl | deleteSize | Deleted size with id: {}", id);
        } catch (SizeNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            log.error("SizeServiceImpl | deleteSize | Database error deleting size with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("SizeServiceImpl | deleteSize | Unexpected error deleting size with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Size findSizeById(String id) {
        return sizeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("SizeServiceImpl | findSizeById | Size not found with id: {}", id);
                    return new SizeNotFoundException(id);
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public boolean doesSizeExistById(String id) {
        return sizeRepository.existsById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void checkSizeNameExists(String name) {
        if (sizeRepository.existsByName(name)) {
            log.error("SizeServiceImpl | checkSizeNameExists | Size already exists with name: {}", name);
            throw new SizeAlreadyExistsException(name);
        }
    }
}