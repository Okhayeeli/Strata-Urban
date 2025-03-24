//package com.strataurban.strata.Configs;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//// RateLimitingFilter.java
//public class RateLimitingFilter extends OncePerRequestFilter {
//    private final CustomRateLimiter rateLimiter;
//
//    @Value("${rate.limiter.cooldown.period.in.seconds}")
//    int cooldownPeriodInSeconds;
//
//    public RateLimitingFilter(CustomRateLimiter rateLimiter) {
//        this.rateLimiter = rateLimiter;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//        String clientId = getClientIdentifier(request);
//
//
//        // Bypass rate limiting for Swagger UI and API docs endpoints
//        if (request.getRequestURI().contains("/swagger-ui") ||
//                request.getRequestURI().contains("/v3/api-docs")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//
//        if (request.getRequestURI().startsWith("/api/auth")) {
//            CustomRateLimiter.RateLimitResult result = rateLimiter.tryAcquire(clientId);
//            if (!result.isAllowed()) {
//                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
//                response.setContentType("application/json");
//                String errorMessage = String.format(
//                        "{\"message\":\"Too many requests. Please wait %d seconds before trying again.\","
//                                + "\"code\":\"RATE_LIMIT_EXCEEDED\","
//                                + "\"waitTimeSeconds\":%d,"
//                                + "\"retryAfter\":%d}",
//                        result.getWaitTimeSeconds(),
//                        result.getWaitTimeSeconds(),
//                        result.getWaitTimeSeconds()
//                );
//                response.setHeader("Retry-After", String.valueOf(result.getWaitTimeSeconds()));
//                response.getWriter().write(errorMessage);
//                return;
//            }
//        }
//        filterChain.doFilter(request, response);
//
//        // Inside your doFilterInternal or similar method in JwtAuthFilter
//        if (request.getRequestURI().contains("/swagger-ui") ||
//                request.getRequestURI().contains("/v3/api-docs")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//    }
//
//    private String getClientIdentifier(HttpServletRequest request) {
//        String clientIp = request.getHeader("X-Forwarded-For");
//        if (clientIp == null || clientIp.isEmpty()) {
//            clientIp = request.getRemoteAddr();
//        }
//        return clientIp;
//    }
//}
