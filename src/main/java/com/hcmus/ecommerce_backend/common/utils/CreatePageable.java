package com.hcmus.ecommerce_backend.common.utils;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Slf4j
public class CreatePageable {
    public static Pageable build(int page, int size, String[] sortParams) {
        log.info("CreatePageable | build | page: {}, size: {}, sortParams: {}", 
                page, size, sortParams != null ? String.join(", ", sortParams) : "unsorted");

        if (sortParams == null || sortParams.length == 0) {
            return PageRequest.of(page, size);
        }
        
        List<Sort.Order> orders = new ArrayList<>();
        
        // If we have exactly 2 parameters and the second one is "asc" or "desc",
        // treat them as a property-direction pair
        if (sortParams.length == 2 && 
            (sortParams[1].equalsIgnoreCase("asc") || sortParams[1].equalsIgnoreCase("desc"))) {
            Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            orders.add(new Sort.Order(direction, sortParams[0]));
            log.debug("CreatePageable | Parsed as property-direction pair: {} {}", sortParams[0], direction);
            return PageRequest.of(page, size, Sort.by(orders));
        }
        
        // Otherwise process each parameter normally
        for (String param : sortParams) {
            // Check if the parameter contains a comma (indicating property,direction format)
            if (param.contains(",")) {
                String[] parts = param.split(",");
                String property = parts[0];
                Sort.Direction direction = Sort.Direction.ASC; // Default direction
                
                // Only check for direction if there's a second part
                if (parts.length > 1) {
                    direction = parts[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
                }
                
                orders.add(new Sort.Order(direction, property));
                log.debug("CreatePageable | Added sort order: {} {}", property, direction);
            } else {
                // If no direction specified, use ascending
                orders.add(new Sort.Order(Sort.Direction.ASC, param));
                log.debug("CreatePageable | Added sort order: {} ASC", param);
            }
        }

        return PageRequest.of(page, size, Sort.by(orders));
    }
}