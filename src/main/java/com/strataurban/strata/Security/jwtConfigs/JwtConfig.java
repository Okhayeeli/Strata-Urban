//package com.strataurban.strata.Security.jwtConfigs;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class JwtConfig {
//    @Value("${jwt.secret:your-secret-key}")
//    private String secret;
//
//    @Value("${jwt.expiration.access:86400000}") // 24 hours in milliseconds
//    private Long accessTokenExpiration;
//
//    @Value("${jwt.expiration.refresh:604800000}") // 7 days in milliseconds
//    private Long refreshTokenExpiration;
//
//    public String getSecret() {
//        return secret;
//    }
//
//    public Long getAccessTokenExpiration() {
//        return accessTokenExpiration;
//    }
//
//    public Long getRefreshTokenExpiration() {
//        return refreshTokenExpiration;
//    }
//}
