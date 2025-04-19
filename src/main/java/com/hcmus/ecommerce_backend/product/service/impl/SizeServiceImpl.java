package com.hcmus.ecommerce_backend.product.service.impl;

import com.hcmus.ecommerce_backend.product.exception.SizeAlreadyExistsException;
import com.hcmus.ecommerce_backend.product.exception.SizeNotFoundException;
import com.hcmus.ecommerce_backend.product.model.dto.request.CreateSizeRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.UpdateSizeRequest;
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

import java.util.Collections;
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
    public Page<SizeResponse> getAllSizes(Pageable pageable) {
        log.info("SizeServiceImpl | getAllSizes | Retrieving sizes with pagination - Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            Page<Size> sizePage = sizeRepository.findAll(pageable);
            Page<SizeResponse> sizeResponsePage = sizePage.map(sizeMapper::toResponse);
            log.info("SizeServiceImpl | getAllSizes | Found {} sizes on page {} of {}",
                    sizeResponsePage.getNumberOfElements(),
                    sizeResponsePage.getNumber() + 1,
                    sizeResponsePage.getTotalPages());
            return sizeResponsePage;
        } catch (DataAccessException e) {
            log.error("SizeServiceImpl | getAllSizes | Database error retrieving paginated sizes: {}", e.getMessage(), e);
            return Page.empty(pageable);
        } catch (Exception e) {
            log.error("SizeServiceImpl | getAllSizes | Unexpected error retrieving paginated sizes: {}", e.getMessage(), e);
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
    private Size findSizeById(String id) {
        return sizeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("SizeServiceImpl | findSizeById | Size not found with id: {}", id);
                    return new SizeNotFoundException(id);
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private boolean doesSizeExistById(String id) {
        return sizeRepository.existsById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    private void checkSizeNameExists(String name) {
        if (sizeRepository.existsByName(name)) {
            log.error("SizeServiceImpl | checkSizeNameExists | Size already exists with name: {}", name);
            throw new SizeAlreadyExistsException(name);
        }
    }
}