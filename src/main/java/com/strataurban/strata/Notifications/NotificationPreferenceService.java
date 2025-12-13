package com.strataurban.strata.Notifications;

import com.strataurban.strata.Notifications.Entities.NotificationPreference;
import com.strataurban.strata.Notifications.Repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing user notification preferences
 * Users select which CHANNELS they want (EMAIL, SMS, PUSH, IN_APP)
 * Once enabled, they receive ALL notification types via that channel
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;

    /**
     * Get all preferences for a user
     * Returns which channels are enabled
     */
    @Transactional(readOnly = true)
    public List<NotificationPreferenceDTO> getUserPreferences(Long userId) {
        log.debug("Fetching preferences for user {}", userId);

        List<NotificationPreference> preferences = preferenceRepository.findByUserId(userId);

        return preferences.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get just the list of enabled channels
     */
    @Transactional(readOnly = true)
    public List<NotificationChannel> getEnabledChannels(Long userId) {
        log.debug("Fetching enabled channels for user {}", userId);
        return preferenceRepository.findEnabledChannels(userId);
    }

    /**
     * Get preferences as a simple map: Channel -> Enabled
     */
    @Transactional(readOnly = true)
    public Map<NotificationChannel, Boolean> getUserPreferencesMap(Long userId) {
        log.debug("Fetching preferences map for user {}", userId);

        List<NotificationPreference> preferences = preferenceRepository.findByUserId(userId);

        Map<NotificationChannel, Boolean> map = new EnumMap<>(NotificationChannel.class);

        // Initialize all channels as disabled
        for (NotificationChannel channel : NotificationChannel.values()) {
            map.put(channel, false);
        }

        // Update with actual preferences
        for (NotificationPreference pref : preferences) {
            map.put(pref.getChannel(), pref.isEnabled());
        }

        return map;
    }

    /**
     * Update user's preference for a specific channel
     * This affects ALL notification types
     */
    @Transactional
    public NotificationPreferenceDTO updatePreference(
            Long userId,
            NotificationChannel channel,
            boolean enabled) {

        log.info("Updating preference for user {}: {} = {}", userId, channel, enabled);

        Optional<NotificationPreference> existing = preferenceRepository
                .findByUserIdAndChannel(userId, channel);

        NotificationPreference preference;

        if (existing.isPresent()) {
            preference = existing.get();
            preference.setEnabled(enabled);
        } else {
            preference = NotificationPreference.builder()
                    .userId(userId)
                    .channel(channel)
                    .enabled(enabled)
                    .build();
        }

        preference = preferenceRepository.save(preference);
        log.info("Preference updated successfully for user {}: {} = {}", userId, channel, enabled);

        return toDTO(preference);
    }

    /**
     * Bulk update preferences for a user
     */
    @Transactional
    public List<NotificationPreferenceDTO> updatePreferences(
            Long userId,
            Map<NotificationChannel, Boolean> preferences) {

        log.info("Bulk updating preferences for user {}: {}", userId, preferences);

        List<NotificationPreference> entities = new ArrayList<>();

        for (Map.Entry<NotificationChannel, Boolean> entry : preferences.entrySet()) {
            Optional<NotificationPreference> existing = preferenceRepository
                    .findByUserIdAndChannel(userId, entry.getKey());

            NotificationPreference entity;
            if (existing.isPresent()) {
                entity = existing.get();
                entity.setEnabled(entry.getValue());
            } else {
                entity = NotificationPreference.builder()
                        .userId(userId)
                        .channel(entry.getKey())
                        .enabled(entry.getValue())
                        .build();
            }
            entities.add(entity);
        }

        List<NotificationPreference> saved = preferenceRepository.saveAll(entities);
        log.info("Successfully updated {} preferences for user {}", saved.size(), userId);

        return saved.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Enable a specific channel for a user
     */
    @Transactional
    public void enableChannel(Long userId, NotificationChannel channel) {
        log.info("Enabling channel {} for user {}", channel, userId);
        updatePreference(userId, channel, true);
    }

    /**
     * Disable a specific channel for a user
     */
    @Transactional
    public void disableChannel(Long userId, NotificationChannel channel) {
        log.info("Disabling channel {} for user {}", channel, userId);
        updatePreference(userId, channel, false);
    }

    /**
     * Enable multiple channels at once
     */
    @Transactional
    public void enableChannels(Long userId, List<NotificationChannel> channels) {
        log.info("Enabling {} channels for user {}: {}", channels.size(), userId, channels);

        for (NotificationChannel channel : channels) {
            updatePreference(userId, channel, true);
        }
    }

    /**
     * Disable all notifications for a user
     */
    @Transactional
    public void disableAllNotifications(Long userId) {
        log.info("Disabling all notifications for user {}", userId);

        List<NotificationPreference> preferences = preferenceRepository.findByUserId(userId);
        preferences.forEach(p -> p.setEnabled(false));

        preferenceRepository.saveAll(preferences);
        log.info("Disabled all notifications for user {}", userId);
    }

    /**
     * Initialize default preferences for a new user
     * By default, enable only IN_APP notifications
     * Total: 4 records per user (one for each channel)
     */
    @Transactional
    public void initializeDefaultPreferences(Long userId) {
        log.info("Initializing default preferences for user {}", userId);

        List<NotificationPreference> preferences = new ArrayList<>();

        // Enable IN_APP by default
        preferences.add(NotificationPreference.builder()
                .userId(userId)
                .channel(NotificationChannel.IN_APP)
                .enabled(true)
                .build());

        // Disable other channels by default
        preferences.add(NotificationPreference.builder()
                .userId(userId)
                .channel(NotificationChannel.EMAIL)
                .enabled(true)
                .build());

        preferences.add(NotificationPreference.builder()
                .userId(userId)
                .channel(NotificationChannel.SMS)
                .enabled(false)
                .build());

        preferences.add(NotificationPreference.builder()
                .userId(userId)
                .channel(NotificationChannel.PUSH)
                .enabled(false)
                .build());

        preferenceRepository.saveAll(preferences);
        log.info("Initialized default preferences for user {} - 4 records created", userId);
    }

    /**
     * Check if a specific channel is enabled
     */
    @Transactional(readOnly = true)
    public boolean isChannelEnabled(Long userId, NotificationChannel channel) {
        return preferenceRepository.isChannelEnabled(userId, channel);
    }

    /**
     * Delete all preferences for a user
     */
    @Transactional
    public void deleteUserPreferences(Long userId) {
        log.info("Deleting all preferences for user {}", userId);
        preferenceRepository.deleteByUserId(userId);
        log.info("Deleted all preferences for user {}", userId);
    }

    /**
     * Convert entity to DTO
     */
    private NotificationPreferenceDTO toDTO(NotificationPreference preference) {
        return NotificationPreferenceDTO.builder()
                .id(preference.getId())
                .userId(preference.getUserId())
                .channel(preference.getChannel())
                .enabled(preference.isEnabled())
                .createdAt(preference.getCreatedAt())
                .updatedAt(preference.getUpdatedAt())
                .build();
    }
}