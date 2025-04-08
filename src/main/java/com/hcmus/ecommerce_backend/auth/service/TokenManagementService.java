package com.hcmus.ecommerce_backend.auth.service;

import java.util.Set;

public interface TokenManagementService {
    void invalidateTokens(Set<String> tokenIds);
    boolean checkForInvalidityOfToken(String tokenId);
}