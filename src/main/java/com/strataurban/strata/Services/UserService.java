package com.strataurban.strata.Services;

import com.strataurban.strata.Entities.Generics.PasswordResetToken;
import com.strataurban.strata.Entities.Providers.Supplier;
import com.strataurban.strata.Enums.EnumRoles;
import com.strataurban.strata.Enums.Provider;
import com.strataurban.strata.Repositories.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Repositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.strataurban.strata.Enums.Provider.LOCAL;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final SupplierRepository supplierRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailService emailService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Set default values for UserDetails implementation
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        // Set default provider if not specified
        if (user.getProvider() == null) {
            user.setProvider(Provider.LOCAL);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save as Supplier if role is SUPPLIER
        if (EnumRoles.SUPPLIER.equals(user.getRoles())) {
            Supplier supplier = new Supplier();
            mapToSupplier(user, supplier); // Map fields from User to Supplier
            return supplierRepository.save(supplier); // Save Supplier using SupplierRepository
        }

        // Otherwise, save as regular User (Client)
        return userRepository.save(user);
    }


    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PasswordResetToken token = passwordResetTokenService.createToken(user.getId());
        // Send email with reset link
        emailService.sendPasswordResetEmail(user.getEmail(), token.getToken());
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenService.findByToken(token);
        passwordResetTokenService.validateToken(resetToken);

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private void mapToSupplier(User source, Supplier supplier) {
        supplier.setUsername(source.getUsername());
        supplier.setEmail(source.getEmail());
        supplier.setPassword(source.getPassword());
        supplier.setEnabled(source.isEnabled());
        supplier.setAccountNonExpired(source.isAccountNonExpired());
        supplier.setAccountNonLocked(source.isAccountNonLocked());
        supplier.setCredentialsNonExpired(source.isCredentialsNonExpired());
        supplier.setRoles(source.getRoles());
        supplier.setProvider(source.getProvider());
        supplier.setPreferredLanguage(source.getPreferredLanguage());

        // Supplier-specific fields from input
        supplier.setSupplierCode(generateSupplierCode());
        supplier.setFirstName(source.getFirstName()); // Assuming these fields are part of User
        supplier.setMiddleName(source.getMiddleName());
        supplier.setLastName(source.getLastName());
        supplier.setPhoneNumber(source.getPhone());
        supplier.setAddress(source.getAddress());
        supplier.setCity(source.getCity());
        supplier.setState(source.getState());
        supplier.setCountry(source.getCountry());
    }


    private String generateSupplierCode() {
        return "SUP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}