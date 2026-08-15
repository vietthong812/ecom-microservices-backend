package com.example.api_gateway.util;
import com.example.api_gateway.entity.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
@RequiredArgsConstructor
public class JwtUtil{

    private final JwtProperties jwtProperties;
    
    public Claims validateToken(final String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token).getPayload();
    }
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUserIdFromToken(String authHeader) {
        return validateToken(authHeader.replace("Bearer ", "")).get("userId", String.class);
    }
    public String getUsernameFromToken(String authHeader) {
        return validateToken(authHeader.replace("Bearer ", "")).get("username", String.class);
    }
    public String getRolesFromToken(String authHeader) {
        return validateToken(authHeader.replace("Bearer ", "")).get("role", String.class);
    }
}
