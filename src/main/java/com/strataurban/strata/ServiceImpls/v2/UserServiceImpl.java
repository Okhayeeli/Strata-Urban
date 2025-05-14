package com.strataurban.strata.ServiceImpls.v2;

import com.strataurban.strata.DTOs.v2.*;
import com.strataurban.strata.Entities.CustomUserDetails;
import com.strataurban.strata.Entities.Generics.BlacklistedToken;
import com.strataurban.strata.Entities.Passengers.Client;
import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.Providers.ServiceArea;
import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Enums.EnumRoles;
import com.strataurban.strata.Repositories.v2.ServiceAreaRepository;
import com.strataurban.strata.Repositories.v2.UserRepository;
import com.strataurban.strata.Security.jwtConfigs.JwtUtil;
import com.strataurban.strata.Services.v2.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.strataurban.strata.Repositories.v2.BlacklistedTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;

import java.time.LocalDateTime;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ServiceAreaRepository serviceAreaRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    private static final int MAX_PASSWORD_HISTORY = 5;
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    // Password complexity requirements
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{" + MIN_PASSWORD_LENGTH + ",}$"
    );

    @Autowired
    public UserServiceImpl(
            UserRepository userRepository,
            ServiceAreaRepository serviceAreaRepository,
            BlacklistedTokenRepository blacklistedTokenRepository,
            PasswordEncoder passwordEncoder,
            @Lazy AuthenticationManager authenticationManager,
            JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.serviceAreaRepository = serviceAreaRepository;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Client registerClient(ClientRegistrationRequest request) {
        if (userRepository.findByUsernameOrEmail(request.getUsername(), request.getEmail()).isPresent()) {
            throw new RuntimeException("Username or email already exists");
        }

        validatePassword(request.getPassword());

        Client client = new Client();
        client.setTitle(request.getTitle());
        client.setFirstName(request.getFirstName());
        client.setMiddleName(request.getMiddleName());
        client.setLastName(request.getLastName());
        client.setEmail(request.getEmail());
        client.setUsername(request.getUsername());
        client.setPassword(passwordEncoder.encode(request.getPassword()));
        client.setPhone(request.getPhone());
        client.setPhone2(request.getPhone2());
        client.setAddress(request.getAddress());
        client.setPreferredLanguage(request.getPreferredLanguage());
        client.setCity(request.getCity());
        client.setState(request.getState());
        client.setCountry(request.getCountry());
        client.setImageUrl(request.getImageUrl());
        client.setRoles(EnumRoles.CLIENT);
        client.setEmailVerified(false);

        return (Client) userRepository.save(client);
    }

    @Override
    public Provider registerProvider(ProviderRegistrationRequest request) {
        if (userRepository.findByUsernameOrEmail(request.getUsername(), request.getEmail()).isPresent()) {
            throw new RuntimeException("Username or email already exists");
        }

        validatePassword(request.getPassword());

        Provider provider = new Provider();
        provider.setTitle(request.getTitle());
        provider.setFirstName(request.getFirstName());
        provider.setMiddleName(request.getMiddleName());
        provider.setLastName(request.getLastName());
        provider.setEmail(request.getEmail());
        provider.setUsername(request.getUsername());
        provider.setPassword(passwordEncoder.encode(request.getPassword()));
        provider.setPhone(request.getPhone());
        provider.setPhone2(request.getPhone2());
        provider.setAddress(request.getAddress());
        provider.setPreferredLanguage(request.getPreferredLanguage());
        provider.setCity(request.getCity());
        provider.setState(request.getState());
        provider.setCountry(request.getCountry());
        provider.setImageUrl(request.getImageUrl());
        provider.setCompanyLogoUrl(request.getCompanyLogoUrl());
        provider.setPrimaryContactPosition(request.getPrimaryContactPosition());
        provider.setPrimaryContactDepartment(request.getPrimaryContactDepartment());
        provider.setCompanyBannerUrl(request.getCompanyBannerUrl());
        provider.setSupplierCode(request.getSupplierCode());
        provider.setCompanyName(request.getCompanyName());
        provider.setCompanyAddress(request.getCompanyAddress());
        provider.setCompanyRegistrationNumber(request.getCompanyRegistrationNumber());
        provider.setCompanyBusinessEmail(request.getCompanyBusinessEmail());
        provider.setCompanyBusinessPhone(request.getCompanyBusinessPhone());
        provider.setCompanyBusinessWebsite(request.getCompanyBusinessWebsite());
        provider.setCompanyBusinessType(request.getCompanyBusinessType());
        provider.setDescription(request.getDescription());
        provider.setZipCode(request.getZipCode());
        provider.setServiceTypes(request.getServiceTypes());
        provider.setRoles(EnumRoles.PROVIDER);
        provider.setEmailVerified(false);

        if (request.getServiceAreaIds() != null && !request.getServiceAreaIds().isEmpty()) {
            List<ServiceArea> serviceAreas = serviceAreaRepository.findAllById(request.getServiceAreaIds());
            if (serviceAreas.size() != request.getServiceAreaIds().size()) {
                throw new RuntimeException("One or more service area IDs are invalid");
            }
            String serviceAreasString = request.getServiceAreaIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            provider.setServiceAreas(serviceAreasString);
        }

        return (Provider) userRepository.save(provider);
    }

    @Override
    public User registerInternalUser(AdminRegistrationRequest request) {
        if (!List.of(EnumRoles.ADMIN, EnumRoles.CUSTOMER_SERVICE, EnumRoles.DEVELOPER).contains(request.getRole())) {
            throw new RuntimeException("Invalid role for internal user registration. Must be ADMIN, CUSTOMER_SERVICE, or DEVELOPER.");
        }

        if (userRepository.findByUsernameOrEmail(request.getUsername(), request.getEmail()).isPresent()) {
            throw new RuntimeException("Username or email already exists");
        }

        validatePassword(request.getPassword());
        User user = new User();
        user.setTitle(request.getTitle());
        user.setFirstName(request.getFirstName());
        user.setMiddleName(request.getMiddleName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setPhone2(request.getPhone2());
        user.setAddress(request.getAddress());
        user.setPreferredLanguage(request.getPreferredLanguage());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setCountry(request.getCountry());
        user.setImageUrl(request.getImageUrl());
        user.setRoles(request.getRole());
        user.setEmailVerified(true);

        return userRepository.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.debug("Attempting login for: {}", loginRequest.getUsernameOrEmail());
        User user = userRepository.findByUsernameOrEmail(
                loginRequest.getUsernameOrEmail(),
                loginRequest.getUsernameOrEmail()
        ).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()
                    )
            );
            log.debug("Authentication successful: {}", authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);


            String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRoles().name());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
//            String newJti = jwtUtil.getJtiFromToken(refreshToken);


            // Reset failed login attempts on successful login
//            user.setCurrentRefreshTokenJti(newJti);
            resetFailedLoginAttempts(user);
//            userRepository.save(user);


            LoginResponse response = new LoginResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            log.debug("Login response: accessToken={}, refreshToken={}", accessToken, refreshToken);
            return response;
        } catch (BadCredentialsException e) {
            // Increment failed login attempts on authentication failure
            incrementFailedLoginAttempts(user);
            throw new RuntimeException("Invalid credentials", e);
        }
    }

    @Override
    public void logout(String refreshToken) {
        if (jwtUtil.validateToken(refreshToken)) {
            String jti = jwtUtil.getJtiFromToken(refreshToken);
//            User user = userRepository.findByCurrentRefreshTokenJti(jti)
//                    .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
            BlacklistedToken blacklistedToken = new BlacklistedToken();
            blacklistedToken.setJti(jti);
            blacklistedToken.setBlacklistedAt(LocalDateTime.now());
            blacklistedToken.setExpiresAt(LocalDateTime.now().plusDays(7));
            blacklistedTokenRepository.save(blacklistedToken);
//            user.setCurrentRefreshTokenJti(null);
//            userRepository.save(user);
        } else {
            throw new RuntimeException("Invalid refresh token");
        }
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }

    @Override
    public User updateUser(Long id, User user) {
        User existingUser = getUserById(id);
        existingUser.setTitle(user.getTitle());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setMiddleName(user.getMiddleName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setPhone2(user.getPhone2());
        existingUser.setAddress(user.getAddress());
        existingUser.setCity(user.getCity());
        existingUser.setState(user.getState());
        existingUser.setCountry(user.getCountry());
        existingUser.setPreferredLanguage(user.getPreferredLanguage());
        existingUser.setImageUrl(user.getImageUrl());
        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }

    @Override
    public void verifyEmail(Long id, String token) {
        User user = getUserById(id);
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Override
    public void changePassword(Long id, ChangePasswordRequest request) {
        User user = getUserById(id);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        validatePassword(request.getNewPassword());
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> searchUsers(String query) {
        return userRepository.searchUsers(query);
    }

    @Override
    public List<User> getUsersByRole(EnumRoles role) {
        return userRepository.findByRoles(role);
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.info("Loading user: {}", usernameOrEmail);

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));
        log.info("Found user: username={}, password={}, roles={}", user.getUsername(), user.getPassword(), user.getRoles());
        return new CustomUserDetails(user);
    }


    public void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH +
                            " characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&)"
            );
        }
    }

    @Override
    public void incrementFailedLoginAttempts(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
            log.warn("Account locked for user: {} due to {} failed login attempts", user.getUsername(), attempts);
        }
        userRepository.save(user);
    }

    @Override
    public void resetFailedLoginAttempts(User user) {
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        userRepository.save(user);
    }

    @Override
    public List<User> getClientsInProviderServiceArea(Long providerId) {
        User user = getUserById(providerId);
        if (!EnumRoles.PROVIDER.equals(user.getRoles())) {
            throw new RuntimeException("User is not a PROVIDER");
        }

        Provider provider = (Provider) user;
        if (provider.getServiceAreas() == null || provider.getServiceAreas().isEmpty()) {
            return List.of();
        }

        List<Long> serviceAreaIds = Arrays.stream(provider.getServiceAreas().split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        return userRepository.findByRoles(EnumRoles.CLIENT).stream()
                .filter(client -> {
                    String clientCity = client.getCity();
                    return serviceAreaRepository.findAllById(serviceAreaIds).stream()
                            .anyMatch(area -> area.getName().equalsIgnoreCase(clientCity));
                })
                .collect(Collectors.toList());
    }

    @Override
    public void updateSessionTimeout(Long userId, int timeoutMinutes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setPreferredSessionTimeoutMinutes(timeoutMinutes);
        userRepository.save(user);
    }

    @Override
    public User enableUser(UserDTO userDTO) {
        User user = null;

        // Check id, email, or username in order of preference
        if (userDTO.getId() != null) {
            user = userRepository.findById(userDTO.getId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userDTO.getId()));
        } else if (userDTO.getEmail() != null) {
            user = userRepository.findByEmail(userDTO.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + userDTO.getEmail()));
        } else if (userDTO.getUsername() != null) {
            user = userRepository.findByUsername(userDTO.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found with username: " + userDTO.getUsername()));
        } else {
            throw new IllegalArgumentException("At least one of id, email, or username must be provided");
        }

        // Update emailVerified field
        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            user = userRepository.save(user);
            log.info("User enabled successfully: {}", user.getUsername());
        } else {
            log.info("User is already enabled: {}", user.getUsername());
        }

        return user;
    }

    // Update last activity on every request (via a filter or service call)
    public void updateLastActivity(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setLastActivity(LocalDateTime.now());
        userRepository.save(user);
    }

    // Check if session is expired due to inactivity
    public boolean isSessionExpired(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (user.getLastActivity() == null) return false; // No activity tracked yet
        LocalDateTime timeout = user.getLastActivity().plusMinutes(user.getPreferredSessionTimeoutMinutes());
        boolean expired = LocalDateTime.now().isAfter(timeout);
        if (expired) {
            if (user.getCurrentRefreshTokenJti() != null) {
                BlacklistedToken blacklistedToken = new BlacklistedToken();
                blacklistedToken.setJti(user.getCurrentRefreshTokenJti());
                blacklistedToken.setBlacklistedAt(LocalDateTime.now());
                blacklistedToken.setExpiresAt(LocalDateTime.now().plusDays(7));
                blacklistedTokenRepository.save(blacklistedToken);
                user.setCurrentRefreshTokenJti(null);
                userRepository.save(user);
            }
        }
        return expired;
    }

    public User findUserByUsername(String username) {
      return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
