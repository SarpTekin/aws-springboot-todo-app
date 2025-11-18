package com.microtodo.user_service.util;

import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Helper class to generate test JWT tokens for integration tests
 */
public class TestJwtHelper {
    
    // Use the same secret as in application.properties
    private static final String SECRET = "WGhzVGZqNXdKSmJvcG1lT1BoU3pBRFNlcnZKeWNYR1c=";
    
    /**
     * Generate a test JWT token for a given user
     * @param userId The user ID to include in the token
     * @param username The username to include in the token
     * @return A valid JWT token string
     */
    public static String generateTestToken(Long userId, String username) {
        long expiration = 3600000; // 1 hour
        
        return Jwts.builder()
                .setClaims(Map.of("userId", userId))
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }
    
    private static SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

