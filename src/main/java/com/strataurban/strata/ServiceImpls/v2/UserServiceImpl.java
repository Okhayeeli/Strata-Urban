package com.strataurban.strata.ServiceImpls.v2;

import com.strataurban.strata.DTOs.v2.*;
import com.strataurban.strata.Entities.Generics.BlacklistedToken;
import com.strataurban.strata.Entities.Passengers.Client;
import com.strataurban.strata.Entities.Providers.Provider;
import com.strataurban.strata.Entities.Providers.ServiceArea;
import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Enums.EnumRoles;
import com.strataurban.strata.Repositories.v2.ServiceAreaRepository;
import com.strataurban.strata.Repositories.v2.UserRepository;
import com.strataurban.strata.Security.jwtConfigs.JwtConfig;
import com.strataurban.strata.Services.v2.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import com.strataurban.strata.DTOs.v2.*;
import com.strataurban.strata.Repositories.v2.BlacklistedTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ServiceAreaRepository serviceAreaRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtConfig jwtUtil;

    @Autowired
    public UserServiceImpl(
            UserRepository userRepository,
            ServiceAreaRepository serviceAreaRepository,
            BlacklistedTokenRepository blacklistedTokenRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtConfig jwtUtil) {
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
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsernameOrEmail(
                loginRequest.getUsernameOrEmail(),
                loginRequest.getUsernameOrEmail()
        ).orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), user.getRoles().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        return response;
    }

    @Override
    public void logout(String refreshToken) {
        if (jwtUtil.validateToken(refreshToken)) {
            String jti = jwtUtil.getJtiFromToken(refreshToken);
            BlacklistedToken blacklistedToken = new BlacklistedToken();
            blacklistedToken.setJti(jti);
            blacklistedToken.setBlacklistedAt(LocalDateTime.now());
            blacklistedToken.setExpiresAt(LocalDateTime.now().plusDays(7)); // Match refresh token expiration
            blacklistedTokenRepository.save(blacklistedToken);
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
}
