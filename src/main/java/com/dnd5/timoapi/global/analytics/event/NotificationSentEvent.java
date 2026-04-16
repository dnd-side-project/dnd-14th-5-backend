package com.dnd5.timoapi.global.analytics.event;

public record NotificationSentEvent(Long userId, int tokenCount) {
}
