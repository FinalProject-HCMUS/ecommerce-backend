package com.hcmus.ecommerce_backend.auth.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.hcmus.ecommerce_backend.auth.model.entity.InvalidToken;

import org.springframework.data.jpa.repository.JpaRepository;


@Repository
public interface InvalidTokenRepository extends JpaRepository<InvalidToken, String> {
    Optional<InvalidToken> findByTokenId(final String tokenId);
}