package com.strataurban.strata.Notifications;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceDTO {

    private Long id;
    private Long userId;
    private NotificationChannel channel;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}