package com.strataurban.strata.Notifications;

import com.strataurban.strata.Entities.User;
import com.strataurban.strata.Repositories.v2.ClientRepository;
import com.strataurban.strata.Repositories.v2.ProviderRepository;
import com.strataurban.strata.Repositories.v2.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service to fetch user contact information
 * This is a placeholder - you'll need to implement based on your User entity structure
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserContactService {

    // TODO: Inject your UserRepository or ClientRepository/ProviderRepository here
     private final UserRepository userRepository;
     private final ClientRepository clientRepository;
     private final ProviderRepository providerRepository;

    public record UserContact(
            String email,
            String phoneNumber,
            String deviceToken
    ) {}

    /**
     * Fetch user contact details by user ID
     *
     * TODO: Implement this based on your User entity structure
     * You might need to query different repositories based on RecipientType
     */
    public UserContact getUserContact(Long userId) {
        log.debug("Fetching contact information for user {}", userId);

        try {

             User user = userRepository.findById(userId)
                 .orElseThrow(() -> new RuntimeException("User not found: " + userId));

             return new UserContact(
                 user.getEmail(),
                 user.getPhone(),
                 user.getDeviceToken()
             );

        } catch (Exception e) {
            log.error("Error fetching contact for user {}: {}", userId, e.getMessage(), e);
            return new UserContact(null, null, null);
        }
    }

    /**
     * Update user's device token (for push notifications)
     */
    public void updateDeviceToken(Long userId, String deviceToken) {
        log.info("Updating device token for user {}", userId);

        try {
            // TODO: Implement device token update
             User user = userRepository.findById(userId)
                 .orElseThrow(() -> new RuntimeException("User not found: " + userId));
             user.setDeviceToken(deviceToken);
             userRepository.save(user);

            log.info("Device token updated successfully for user {}", userId);
        } catch (Exception e) {
            log.error("Failed to update device token for user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Validate if user has email configured
     */
    public boolean hasEmail(Long userId) {
        UserContact contact = getUserContact(userId);
        return contact.email() != null && !contact.email().trim().isEmpty();
    }

    /**
     * Validate if user has phone number configured
     */
    public boolean hasPhoneNumber(Long userId) {
        UserContact contact = getUserContact(userId);
        return contact.phoneNumber() != null && !contact.phoneNumber().trim().isEmpty();
    }

    /**
     * Validate if user has device token configured
     */
    public boolean hasDeviceToken(Long userId) {
        UserContact contact = getUserContact(userId);
        return contact.deviceToken() != null && !contact.deviceToken().trim().isEmpty();
    }
}