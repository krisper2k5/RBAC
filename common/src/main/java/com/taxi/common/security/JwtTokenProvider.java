package com.taxi.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey key;
    private final long validityInMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration:86400000}") long validityInMs) {

        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must be at least 32 characters for HS256. Got: " +
                            (secret != null ? secret.length() : "null"));
        }

        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validityInMs = validityInMs;

        log.info("✅ JwtTokenProvider initialized | Secret hash: {} | Expiration: {} ms",
                System.identityHashCode(this.key), validityInMs);
    }

    public String createToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            log.debug("🔍 Validating token | Key hash: {} | Token preview: {}",
                    System.identityHashCode(key),
                    token != null && token.length() > 40 ? token.substring(0, 40) + "..." : token);

            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            log.debug("✅ Token validated successfully");
            return true;

        } catch (ExpiredJwtException e) {
            log.error("❌ ExpiredJwtException: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("❌ UnsupportedJwtException: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("❌ MalformedJwtException: {} | Raw: {}", e.getMessage(),
                    token != null ? token.substring(0, Math.min(100, token.length())) : "null");
            return false;
        } catch (SignatureException e) {
            log.error("❌ SIGNATURE MISMATCH! Token signed with different key.");
            return false;
        } catch (IllegalArgumentException e) {
            log.error("❌ IllegalArgumentException: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("❌ UNEXPECTED ERROR: {} | Type: {}", e.getMessage(), e.getClass().getName(), e);
            return false;
        }
    }

    public String getUsername(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public String getRole(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }
}