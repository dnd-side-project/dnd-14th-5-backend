package com.dnd5.timoapi.domain.notification.presentation.response;

import com.dnd5.timoapi.domain.notification.domain.model.NotificationHistory;
import java.time.LocalDateTime;

public record NotificationHistoryResponse(
        Long id,
        String title,
        LocalDateTime notifiedAt
) {

    public static NotificationHistoryResponse from(NotificationHistory model) {
        return new NotificationHistoryResponse(
                model.id(),
                model.title(),
                model.notifiedAt()
        );
    }
}
