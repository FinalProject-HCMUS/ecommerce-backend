package com.hcmus.ecommerce_backend.auth.service.impl;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.hcmus.ecommerce_backend.auth.model.enums.TokenClaims;
import com.hcmus.ecommerce_backend.auth.service.TokenService;
import com.hcmus.ecommerce_backend.auth.service.TokenValidationService;
import com.hcmus.ecommerce_backend.config.TokenConfigurationParameter;
import com.hcmus.ecommerce_backend.user.model.enums.Role;

import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {
        private final TokenConfigurationParameter tokenConfigurationParameter;
        private final TokenValidationService tokenValidationService;

        @Override
        @Cacheable(value = "tokenCache", key = "#token")
        public UsernamePasswordAuthenticationToken getAuthentication(final String token) {
                log.debug("TokenServiceImpl | getAuthentication | token: {}", token);
                try {
                        if(!tokenValidationService.verifyAndValidate(token)) {
                                throw new JwtException("Invalid JWT token");
                        }
                        final Jws<Claims> claimsJws = Jwts.parserBuilder()
                                        .setSigningKey(tokenConfigurationParameter.getPublicKey()).build()
                                        .parseClaimsJws(token);
                        final JwsHeader<?> jwsHeader = claimsJws.getHeader();
                        final Claims payload = claimsJws.getBody();

                        final Jwt jwt = new Jwt(token, payload.getIssuedAt().toInstant(),
                                        payload.getExpiration().toInstant(),
                                        Map.of(TokenClaims.TYP.getValue(), jwsHeader.getType(),
                                                        TokenClaims.ALGORITHM.getValue(), jwsHeader.getAlgorithm()),
                                        payload);

                        final String userRole = payload.get(TokenClaims.USER_ROLE.getValue(), String.class);

                        final ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>();
                        if (userRole != null) {
                                authorities.add(new SimpleGrantedAuthority(userRole));
                        } else {
                                authorities.add(new SimpleGrantedAuthority(Role.USER.name()));
                        }

                        return new UsernamePasswordAuthenticationToken(jwt, null, authorities);
                } catch (JwtException e) {
                        log.error("TokenServiceImpl | getAuthentication | Error parsing token: {}", e.getMessage(), e);
                        throw new JwtException("Invalid JWT token");
                } catch (Exception e) {
                        log.error("TokenServiceImpl | getAuthentication | Error parsing token: {}", e.getMessage(), e);
                        throw new RuntimeException("Invalid token", e);
                }
        }
}