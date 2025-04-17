package com.hcmus.ecommerce_backend.common.model.entity;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.hcmus.ecommerce_backend.auth.model.enums.TokenClaims;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity {

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

    /**
     * Sets the createdBy and createdAt fields before persisting the entity.
     * If no authenticated user is found, sets createdBy to "anonymousUser".
     */
    @PrePersist
    public void prePersist() {
        this.createdBy = getCurrentUserIdentifier();
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedBy = getCurrentUserIdentifier();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Gets the current user identifier from the security context.
     * Handles different authentication principal types.
     * 
     * @return the user identifier or "anonymousUser" if none is available
     */
    private String getCurrentUserIdentifier() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .map(principal -> {
                    if (principal instanceof Jwt jwt) {
                        try {
                            String email = jwt.getClaimAsString(TokenClaims.USER_EMAIL.getValue());
                            if (email != null && !email.isEmpty()) {
                                return email;
                            }
                            // Fallback to subject if email claim is not available
                            return jwt.getSubject();
                        } catch (Exception e) {
                            // Fallback to subject if there's an issue with the claim
                            return jwt.getSubject();
                        }
                    } else {
                        return principal.toString();
                    }
                })
                .orElse("anonymousUser");
    }
}