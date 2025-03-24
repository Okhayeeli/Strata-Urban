//package com.strataurban.strata.Configs;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RateLimitingConfig {
//
//    @Value("${rate.limiter.max.requests}")
//    int maxRequests;
//
//    @Value("${rate.limiter.time.window.in.seconds}")
//    int windowInSeconds;
//
//    @Value("${rate.limiter.cooldown.period.in.seconds}")
//    int cooldownPeriodInSeconds;
//
//    @Bean
//    public CustomRateLimiter authRateLimiter() {
//        return new CustomRateLimiter(maxRequests, windowInSeconds, cooldownPeriodInSeconds);
//    }
//}