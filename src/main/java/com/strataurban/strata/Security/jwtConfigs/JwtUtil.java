package com.strataurban.strata.Security.jwtConfigs;

import com.strataurban.strata.Entities.User;
import com.strataurban.strata.ServiceImpls.v2.UserServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.accessTokenExpirationMs}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refreshTokenExpirationMs}")
    private long refreshTokenExpirationMs;

    @Autowired
    @Lazy
    private UserServiceImpl userServiceImpl;

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("firstName", user.getFirstName());
        claims.put("lastName", user.getLastName());
        claims.put("role", user.getRoles());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("tokenType", "ACCESS"); // Add token type

        log.debug("Generating access token for user: {}", user.getUsername());
        return createToken(claims, user.getUsername(), accessTokenExpirationMs);
    }

    public String generateRefreshToken(String username) {
        User user = userServiceImpl.findUserByUsername(username);

        // Use refresh token expiration from config as fallback
        int timeoutMinutes = user.getPreferredSessionTimeoutMinutes();
        long expirationMs;

        if (timeoutMinutes > 0) {
            expirationMs = (long) timeoutMinutes * 60 * 1000;
        } else {
            // Fallback to configured refresh token expiration
            expirationMs = refreshTokenExpirationMs;
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "REFRESH"); // Add token type
        claims.put("id", user.getId()); // Add user ID for easier lookup

        log.debug("Generating refresh token for user: {} with expiration: {}ms", username, expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationMs) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getSubject();
    }

    public String getRoleFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    public String getJtiFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getId(); // Use getId() instead of get("jti")
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        Object idObj = claims.get("id");
        if (idObj instanceof Integer) {
            return ((Integer) idObj).longValue();
        }
        return (Long) idObj;
    }

    public String getTokenType(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("tokenType", String.class);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            log.debug("Token validation successful");
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    // Specific validation for refresh tokens
    public boolean validateRefreshToken(String token) {
        if (!validateToken(token)) {
            return false;
        }

        try {
            String tokenType = getTokenType(token);
            if (!"REFRESH".equals(tokenType)) {
                log.error("Token is not a refresh token. Type: {}", tokenType);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Error validating refresh token: {}", e.getMessage());
            return false;
        }
    }

    // Check if token is expired without throwing exception
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }
}