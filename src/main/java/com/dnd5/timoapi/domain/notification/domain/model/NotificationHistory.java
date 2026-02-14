package com.dnd5.timoapi.domain.notification.domain.model;

import java.time.LocalDateTime;

public record NotificationHistory(
        Long id,
        Long userId,
        String title,
        String body,
        boolean isRead,
        LocalDateTime notifiedAt
) {

    public static NotificationHistory create(
            Long userId,
            String title,
            String body
    ) {
        return new NotificationHistory(
                null,
                userId,
                title,
                body,
                false,
                null
        );
    }
}
