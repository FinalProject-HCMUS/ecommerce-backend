package com.hcmus.ecommerce_backend.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hcmus.ecommerce_backend.user.model.entity.VerificationToken;


@Repository
public interface  VerificationTokenRepository extends JpaRepository<VerificationToken, String>{

    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUserId(String userId);
}
