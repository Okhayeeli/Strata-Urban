package com.strataurban.strata.Security;

import com.strataurban.strata.Security.jwtConfigs.JwtAuthenticationFilter;
import com.strataurban.strata.ServiceImpls.v2.UserServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.debug("Creating PasswordEncoder");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            @Lazy UserServiceImpl userService,
            PasswordEncoder passwordEncoder) {
        logger.debug("Creating DaoAuthenticationProvider");
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        logger.debug("Creating AuthenticationManager");
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter, CorsLoggingFilter corsLoggingFilter) throws Exception {
        logger.debug("Configuring SecurityFilterChain");
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .securityMatcher(new AntPathRequestMatcher("/api/**"))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v2/auth/signup/**", "/api/v2/auth/login", "/api/v2/auth/refresh", "/api/v2/auth/**","/api/v2/auth/get-all").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/v2/service-areas/report", "/api/v2/notifications/get-all", "/api/v2/auth/get-all/**").permitAll()

                        // ===== YOCO PAYMENT ENDPOINTS =====
                        // IMPORTANT: Webhook must be publicly accessible (no JWT required)
                        .requestMatchers("/api/payments/webhook").permitAll()

                        // Payment endpoints require authentication
                        .requestMatchers("/api/payments/**").authenticated()
                        .requestMatchers("/api/v2/notifications/preferences/channel").permitAll()
                        // ===================================

                        .anyRequest().authenticated()
                )
                .addFilterBefore(corsLoggingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Separate security filter chain for admin portal with session management
     */
    @Bean
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        logger.debug("Configuring Admin SecurityFilterChain");
        http
                .securityMatcher(new AntPathRequestMatcher("/admin/**"))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login", "/admin/forgot-password", "/admin/reset-password", "/admin/notifications/**").permitAll()
                        .requestMatchers("/admin/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/admin/notifications/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        String hierarchy = "ROLE_ADMIN > ROLE_CUSTOMER_SERVICE\n" +
                "ROLE_CUSTOMER_SERVICE > ROLE_CLIENT\n" +
                "ROLE_ADMIN > ROLE_DEVELOPER\n" +
                "ROLE_ADMIN > ROLE_PROVIDER\n" +
                "ROLE_PROVIDER > ROLE_DRIVER";
        roleHierarchy.setHierarchy(hierarchy);
        return roleHierarchy;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        logger.info("Configuring CORS");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*", "http://localhost:3000", "http://localhost:5500", "https://localhost"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        logger.info("CORS configured with origins: {}", configuration.getAllowedOrigins());
        return source;
    }

    @Component
    public class CorsLoggingFilter extends OncePerRequestFilter {
        private static final Logger logger = LoggerFactory.getLogger(CorsLoggingFilter.class);

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            logger.info("Request: Method={}, URL={}, Origin={}, Access-Control-Request-Method={}, Access-Control-Request-Headers={}",
                    request.getMethod(),
                    request.getRequestURL(),
                    request.getHeader("Origin"),
                    request.getHeader("Access-Control-Request-Method"),
                    request.getHeader("Access-Control-Request-Headers"));
            filterChain.doFilter(request, response);
            logger.info("Response: Status={}, Headers={}", response.getStatus(), response.getHeaderNames());
        }
    }
}