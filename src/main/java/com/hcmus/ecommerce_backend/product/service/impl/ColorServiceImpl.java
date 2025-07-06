package com.hcmus.ecommerce_backend.product.service.impl;

import com.hcmus.ecommerce_backend.product.exception.ColorAlreadyExistsException;
import com.hcmus.ecommerce_backend.product.exception.ColorNotFoundException;
import com.hcmus.ecommerce_backend.product.model.dto.request.color.CreateColorRequest;
import com.hcmus.ecommerce_backend.product.model.dto.request.color.UpdateColorRequest;
import com.hcmus.ecommerce_backend.product.model.dto.response.ColorResponse;
import com.hcmus.ecommerce_backend.product.model.entity.Color;
import com.hcmus.ecommerce_backend.product.model.mapper.ColorMapper;
import com.hcmus.ecommerce_backend.product.repository.ColorRepository;
import com.hcmus.ecommerce_backend.product.service.ColorService;
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
public class ColorServiceImpl implements ColorService {
    private final ColorRepository colorRepository;
    private final ColorMapper colorMapper;
    
    @Override
    @Transactional(readOnly = true)
    public Page<ColorResponse> getAllColors(Pageable pageable) {
        log.info("ColorServiceImpl | getAllColors | Retrieving colors with pagination - Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            Page<Color> colorPage = colorRepository.findAll(pageable);
            Page<ColorResponse> colorResponsePage = colorPage.map(colorMapper::toResponse);
            log.info("ColorServiceImpl | getAllColors | Found {} colors on page {} of {}",
                    colorResponsePage.getNumberOfElements(),
                    colorResponsePage.getNumber() + 1,
                    colorResponsePage.getTotalPages());
            return colorResponsePage;
        } catch (DataAccessException e) {
            log.error("ColorServiceImpl | getAllColors | Database error retrieving paginated colors: {}", e.getMessage(), e);
            return Page.empty(pageable);
        } catch (Exception e) {
            log.error("ColorServiceImpl | getAllColors | Unexpected error retrieving paginated colors: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ColorResponse> searchColors(Pageable pageable, String keyword) {
        log.info("ColorServiceImpl | searchColors | Searching colors with keyword: {}", keyword);
        try {
            Page<Color> colorPage;

            if (keyword != null && !keyword.trim().isEmpty()) {
                colorPage = colorRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
            } else {
                colorPage = colorRepository.findAll(pageable);
            }

            Page<ColorResponse> colorResponsePage = colorPage.map(colorMapper::toResponse);
            log.info("ColorServiceImpl | searchColors | Found {} colors matching keyword '{}'",
                    colorResponsePage.getTotalElements(), keyword);
            return colorResponsePage;
        } catch (DataAccessException e) {
            log.error("ColorServiceImpl | searchColors | Database error searching colors: {}", e.getMessage(), e);
            return Page.empty(pageable);
        } catch (Exception e) {
            log.error("ColorServiceImpl | searchColors | Unexpected error searching colors: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public ColorResponse getColorById(String id) {
        log.info("ColorServiceImpl | getColorById | id: {}", id);
        try {
            Color color = findColorById(id);
            log.info("ColorServiceImpl | getColorById | Color found: {}", color.getName());
            return colorMapper.toResponse(color);
        } catch (ColorNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("ColorServiceImpl | getColorById | Database error for id {}: {}", id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ColorServiceImpl | getColorById | Unexpected error for id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public ColorResponse createColor(CreateColorRequest request) {
        log.info("ColorServiceImpl | createColor | Creating color with name: {}", request.getName());
        try {
            // Check if color with the same name already exists
            checkColorNameExists(request.getName());
            
            Color color = colorMapper.toEntity(request);
            
            Color savedColor = colorRepository.save(color);
            log.info("ColorServiceImpl | createColor | Created color with id: {}", savedColor.getId());
            return colorMapper.toResponse(savedColor);
        } catch (ColorAlreadyExistsException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("ColorServiceImpl | createColor | Database error creating color '{}': {}", 
                    request.getName(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ColorServiceImpl | createColor | Unexpected error creating color '{}': {}", 
                    request.getName(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public List<ColorResponse> createMultipleColors(List<CreateColorRequest> requests) {
        log.info("ColorServiceImpl | createMultipleColors | Creating {} colors", requests.size());
        try {
            // Check if any color names already exist
            for (CreateColorRequest request : requests) {
                checkColorNameExists(request.getName());
            }

            // Convert all to entities
            List<Color> colors = requests.stream()
                    .map(colorMapper::toEntity)
                    .collect(Collectors.toList());

            // Save all colors
            List<Color> savedColors = colorRepository.saveAll(colors);

            // Convert back to responses
            List<ColorResponse> responses = savedColors.stream()
                    .map(colorMapper::toResponse)
                    .collect(Collectors.toList());

            log.info("ColorServiceImpl | createMultipleColors | Created {} colors", responses.size());
            return responses;
        } catch (ColorAlreadyExistsException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("ColorServiceImpl | createMultipleColors | Database error creating multiple colors: {}",
                    e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ColorServiceImpl | createMultipleColors | Unexpected error creating multiple colors: {}",
                    e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public ColorResponse updateColor(String id, UpdateColorRequest request) {
        log.info("ColorServiceImpl | updateColor | Updating color with id: {}", id);
        try {
            Color color = findColorById(id);
            
            // Only check name existence if the name is being changed
            if (!color.getName().equals(request.getName())) {
                checkColorNameExists(request.getName());
            }
            
            colorMapper.updateEntity(request, color);
            Color updatedColor = colorRepository.save(color);
            log.info("ColorServiceImpl | updateColor | Updated color with id: {}", updatedColor.getId());
            return colorMapper.toResponse(updatedColor);
        } catch (ColorNotFoundException | ColorAlreadyExistsException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("ColorServiceImpl | updateColor | Database error updating color with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ColorServiceImpl | updateColor | Unexpected error updating color with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void deleteColor(String id) {
        log.info("ColorServiceImpl | deleteColor | Deleting color with id: {}", id);
        try {
            // Check existence first in a separate transaction
            if (!doesColorExistById(id)) {
                log.error("ColorServiceImpl | deleteColor | Color not found with id: {}", id);
                throw new ColorNotFoundException(id);
            }
            
            // Then delete in the current transaction
            colorRepository.deleteById(id);
            log.info("ColorServiceImpl | deleteColor | Deleted color with id: {}", id);
        } catch (ColorNotFoundException e) {
            throw e; // Re-throw domain exceptions to be handled by global exception handler
        } catch (DataAccessException e) {
            log.error("ColorServiceImpl | deleteColor | Database error deleting color with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("ColorServiceImpl | deleteColor | Unexpected error deleting color with id '{}': {}", 
                    id, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Helper method to find a color by ID.
     * Uses a separate transaction to avoid issues with the main transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Color findColorById(String id) {
        return colorRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("ColorServiceImpl | findColorById | Color not found with id: {}", id);
                    return new ColorNotFoundException(id);
                });
    }
    
    /**
     * Helper method to check if a color exists by ID.
     * Uses a separate transaction to avoid issues with the main transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public boolean doesColorExistById(String id) {
        return colorRepository.existsById(id);
    }
    
    /**
     * Helper method to check if a color name already exists.
     * Throws ColorAlreadyExistsException if the name exists.
     * Uses a separate transaction to avoid issues with the main transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void checkColorNameExists(String name) {
        if (colorRepository.existsByName(name)) {
                log.error("ColorServiceImpl | checkColorNameExists | Color already exists with name: {}", name);
                throw new ColorAlreadyExistsException(name);
        }
    }
}
